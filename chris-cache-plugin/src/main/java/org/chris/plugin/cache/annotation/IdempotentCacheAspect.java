
package org.chris.plugin.cache.annotation;

import com.alibaba.fastjson.JSON;
import org.chris.common.enums.EnumCommomSysErrorCode;
import org.chris.common.exception.BusinessRuntimeException;
import org.chris.common.response.ResponseMsg;
import org.chris.plugin.cache.util.JedisClusterUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * 幂等校验注解实现
 *
 * @author caizq
 * @date 2018/8/16
 * @since v1.0.0
 */
@Slf4j
@Aspect
public class IdempotentCacheAspect {

    public static final String RESULT = "result";
    @Autowired
    private JedisClusterUtil jedisClusterUtil;


    @Pointcut("@annotation(org.chris.plugin.cache.annotation.IdempotentCache)")
    public void idempotentCacheAnnotationPointcut() {
    }

    @Around("idempotentCacheAnnotationPointcut()")
    public Object invokeResourceWithAnnotation(ProceedingJoinPoint pjp) throws Throwable {
        Method originMethod = getMethod(pjp);

        IdempotentCache annotation = originMethod.getAnnotation(IdempotentCache.class);

        String[] uuidNames = annotation.uuidNames();
        String uuid = getUuidValue(pjp, uuidNames);
        Class returnType = originMethod.getReturnType();

        return cacheUuid(pjp, uuid, annotation, returnType);
    }

    private String getUuidValue(ProceedingJoinPoint pjp, String[] uuidNames) throws IllegalAccessException, InvocationTargetException {
        if (uuidNames == null || uuidNames.length == 0) {
            // Should not go through here.
            throw new IllegalStateException("Wrong state for IdempotentCache annotation");
        }

        String uuid = "";
        String tmp;
        Class objClazz = pjp.getArgs()[0].getClass();
        Method[] methods = objClazz.getDeclaredMethods();
        for (String uuidName : uuidNames) {
            for (Method method : methods) {
                if (("get" + uuidName).equalsIgnoreCase(method.getName())) {
                    tmp = method.invoke(pjp.getArgs()[0]).toString();
                    if (StringUtils.isEmpty(tmp)) {
                        throw new IllegalStateException(uuidName + " cannot be null or empty");
                    }
                    uuid += tmp;
                }
            }

        }
        return uuid;
    }

    /**
     * 缓存UUID  默认过期时间为360秒
     *
     * @param pjp        切点
     * @param orderUuid  请求唯一标示
     * @param annotation
     * @param returnType
     * @return
     * @throws Throwable
     */
    private Object cacheUuid(ProceedingJoinPoint pjp, String orderUuid, IdempotentCache annotation, Class returnType) throws Throwable {
        String threadRequestUuid = UUID.randomUUID().toString();
        String lockKey = annotation.cacheKey() + orderUuid;
        try {
            //step1 查看缓存里有没有处理结果,有就立马返回
            String cacheResult = jedisClusterUtil.get(annotation.cacheKey() + orderUuid + RESULT);
            if (!isEmpty(cacheResult)) {
                return JSON.parseObject(cacheResult, returnType);
            }
            //step2 获取分布式锁
            boolean lockFlag = jedisClusterUtil.tryLock(lockKey, threadRequestUuid, annotation.blockTime(), annotation.lockExpiredTime());
            if (true == lockFlag) {
                //step3 再次查询缓存里有没有处理结果,有就立马返回
                cacheResult = jedisClusterUtil.get(annotation.cacheKey() + RESULT);
                if (!isEmpty(cacheResult)) {
                    return JSON.parseObject(cacheResult, returnType);
                }
                //step4 进行业务处理
                Object result = pjp.proceed();
                //step5 缓存处理结果
                jedisClusterUtil.set(annotation.cacheKey() + orderUuid + RESULT, JSON.toJSONString(result), annotation.cacheExpired());
                return result;
            }
            throw BusinessRuntimeException.buildBusyException(EnumCommomSysErrorCode.WAIT_TIMEOUT_ERROR, lockKey);
        } catch (BusinessRuntimeException e) {
            log.error("缓存失败 获取锁失败{}", e);
            return ResponseMsg.buildFailMsg(e);
        } catch (Exception ex) {
            log.error("缓存失败{}", ex);
            return ResponseMsg.buildUnknownFailMsg();
        } finally {
            //删除缓存
            jedisClusterUtil.unLock(lockKey, threadRequestUuid);
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
