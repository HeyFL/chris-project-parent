
package org.chris.plugin.retry.rabbitmq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.chris.plugin.retry.domain.ResponseMsg;
import org.chris.plugin.retry.domain.RetryMsg;
import org.chris.plugin.retry.enums.EnumCommomSysErrorCode;
import org.chris.plugin.retry.exception.BusinessRuntimeException;
import org.chris.plugin.retry.exception.SystemRuntimeException;
import org.chris.plugin.retry.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author caizq
 * @date 2018/9/13
 * @since v1.0.0
 */
@Slf4j
@Service
@ConditionalOnExpression("'${rabbitmq.version.equal.or.larger.than.three.five.eight}'=='true'")
public class RetryPostJsonRequestClient implements IRetryRequestClient {
    @Autowired
    private IRetryProvider retryProvider;

    @Override
    public String reqeust(RetryMsg retryMsg) {
        String result = HttpUtil.sendPostJson(retryMsg.getRequsetUrl(), retryMsg.getData());
        log.info("请求结果{}", result);

        if (StringUtils.isEmpty(result)) {
            throw SystemRuntimeException.buildBusyException(EnumCommomSysErrorCode.REUQEST_URL_ERROR);
        }

        ResponseMsg responseMsg = JSON.parseObject(result, ResponseMsg.class);

        //请求失败
        if (ResponseMsg.RESPONSE_FLAG_FAILURE.equals(responseMsg.getFlag())) {
            //已尝试次数+1
            retryMsg.addCount();
            //如果尝试次数>最大尝试次数,消息丢进死信队列
            if (retryMsg.getTryCount() >= retryMsg.getMaxTry()) {
                throw BusinessRuntimeException.buildBusyException(EnumCommomSysErrorCode.RETRY_MAX_ERROR);
            }
            retryMsg.setDelayTryTime(retryMsg.getDelayTryTime() * 2);
            //下次尝试时间* 2的当前尝试次数幂   如：最大尝试次数为5，那么这里当前尝试次数最多为4 也就是最后一次尝试时间为 retryMsg.getDelayTryTime()*2^4
            //retryMsg.setDelayTryTime(retryMsg.getDelayTryTime() * (2 ^ retryMsg.getTryCount()));
            //过段时间再次重试
            retryProvider.sendToRetryQueue(retryMsg);
            return ResponseMsg.RESPONSE_FLAG_FAILURE;
        }
        return ResponseMsg.RESPONSE_FLAG_SUCCESS;
    }
}
