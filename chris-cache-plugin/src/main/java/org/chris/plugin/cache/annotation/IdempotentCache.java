
package org.chris.plugin.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过分布式锁,在expired时间内,防止重复请求(重复请求时,返回错误的ResponseMsg)
 *
 * 以这种方式能很大程度地保证幂等,但要做到完全保证幂等还是需要使用到数据库的唯一索引校验
 * @author caizq
 * @date 2018/8/16
 * @since v1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IdempotentCache {

    /**
     * 存在哪个redis Key中
     * @return
     */
    String cacheKey();

    /**
     * 入参对象对应的唯一标示, 如: orderDTO的UUID ,那么这里填"UUID"
     * @return
     */
    String[] uuidNames();

    /**
     * 分布式锁 的过期时间, 默认60s
     * @return
     */
    int lockExpiredTime() default 60;

    /**
     * 结果 的过期时间, 默认600s
     *
     * @return
     */
    int cacheExpired() default 600;

    /**
     * 分布式锁 阻塞时间(获取锁最长等待时间) 默认60s
     * 超过时间throw BusinessRuntimeException
     * @return
     */
    long blockTime() default 60L;

}
