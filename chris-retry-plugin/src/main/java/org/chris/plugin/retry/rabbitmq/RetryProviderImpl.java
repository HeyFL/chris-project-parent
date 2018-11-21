
package org.chris.plugin.retry.rabbitmq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.chris.plugin.retry.domain.RetryMsg;
import org.chris.plugin.retry.enums.EnumCommomSysErrorCode;
import org.chris.plugin.retry.exception.BusinessRuntimeException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author caizq
 * @date 2018/9/12
 * @since v1.0.0
 */
@Slf4j
@Component
public class RetryProviderImpl implements IRetryProvider {


    /**
     * 当MQ版本不支持延迟消息的时候(版本<3.5.8 配置rabbitmq.version.equal.or.larger.than.three.five.eight=true)
     * 这里会为空 调用报MQ_ERROR
     */
    @Autowired(required = false)
    @Qualifier("pmsRetryRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendToRetryQueue(RetryMsg retryMsg) {
        if (rabbitTemplate == null) {
            throw new BusinessRuntimeException(EnumCommomSysErrorCode.MQ_ERROR, retryMsg.getData());
        }
        try {
            rabbitTemplate.convertAndSend("COMMON_RETRY_X", "COMMON_RETRY_R", JSON.toJSONString(retryMsg), (message) -> {
                //消息延迟**ms
                message.getMessageProperties().setHeader("x-delay", retryMsg.getDelayTryTime());
                return message;
            });
        } catch (AmqpException e) {
            log.error("补偿消息发送失败,{}{}", retryMsg, e);
            throw new BusinessRuntimeException(EnumCommomSysErrorCode.MQ_ERROR, e);
        }
    }

    /**
     * 重试服务是否被激活, 是否是可用的
     *
     * @return
     */
    @Override
    public boolean isEnable() {
        return rabbitTemplate != null;
    }
}
