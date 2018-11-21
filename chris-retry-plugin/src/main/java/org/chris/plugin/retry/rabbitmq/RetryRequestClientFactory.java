
package org.chris.plugin.retry.rabbitmq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.chris.plugin.retry.domain.EnumRequestType;
import org.chris.plugin.retry.enums.EnumCommomSysErrorCode;
import org.chris.plugin.retry.exception.BusinessRuntimeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author caizq
 * @date 2018/9/13
 * @since v1.0.0
 */
@Slf4j
@Component
@ConditionalOnExpression("'${rabbitmq.version.equal.or.larger.than.three.five.eight}'=='true'")
public class RetryRequestClientFactory {
    @Resource
    public IRetryRequestClient retryPostJsonRequestClient;

    public IRetryRequestClient buildRetryRequestClient(EnumRequestType requestType) {
        switch (requestType) {
            case POST_JSON:
                return retryPostJsonRequestClient;
            default:
                throw BusinessRuntimeException.buildBusyException(EnumCommomSysErrorCode.UNKNOW_ERROR, JSON.toJSONString(requestType));
        }
    }
}
