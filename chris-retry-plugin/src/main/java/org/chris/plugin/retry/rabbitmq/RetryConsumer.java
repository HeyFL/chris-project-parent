
package org.chris.plugin.retry.rabbitmq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.chris.plugin.cache.util.JedisClusterUtil;
import org.chris.plugin.retry.domain.ResponseMsg;
import org.chris.plugin.retry.domain.RetryMsg;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author caizq
 * @date 2018/9/12
 * @since v1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnExpression("'${rabbitmq.version.equal.or.larger.than.three.five.eight}'=='true'")
public class RetryConsumer {
    public static final String PERFIX = "org:chris:retry:cache";
    private static final String DEFAULT_CHARSET = "UTF-8";
    /**********mq工厂******/
    @Resource
    private ConnectionFactory pmsRetryConnectionFactory;

    @Resource
    private RetryRequestClientFactory retryRequestClientFactory;
    @Autowired
    private JedisClusterUtil jedisClusterUtil;

    /**
     * 通用补偿MQ消费者
     *
     * @return container
     */
    @Bean
    public SimpleMessageListenerContainer messageGridRetryQContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(pmsRetryConnectionFactory);
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "COMMON_RETRY_X_DEAD");
        map.put("x-dead-letter-routing-key", "COMMON_RETRY_R_DEAD");
        Queue queue = new Queue("COMMON_RETRY_Q", false, false, false, map);
        container.setQueues(queue);
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(3);
        container.setConcurrentConsumers(3);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            final long deliveryTag = message.getMessageProperties().getDeliveryTag();
            final String body = new String(message.getBody(), DEFAULT_CHARSET);
            boolean isReject = true;
            try {
                log.info("补偿数据{}", body);
                RetryMsg retryMsg = JSON.parseObject(body, RetryMsg.class);
                // 消费数据
                consume(retryMsg);

                //确认消息成功消费
                channel.basicAck(deliveryTag, false);
                isReject = false;
            } catch (Exception e) {
                log.error("延迟队列消费异常 body:{}", body, e);
            } finally {
                if (isReject) {
                    channel.basicReject(deliveryTag, false);
                }
            }
        });
        return container;
    }

    private void consume(RetryMsg retryMsg) {
        //处理过不再处理
        if (!StringUtils.isEmpty(jedisClusterUtil.get(PERFIX + retryMsg.getMsgUUID()))) {
            return;
        }

        //处理请求
        IRetryRequestClient retryRequestClient = retryRequestClientFactory.buildRetryRequestClient(retryMsg.getRequestType());
        String result = retryRequestClient.reqeust(retryMsg);

        //缓存结果 防止重复处理
        if (ResponseMsg.RESPONSE_FLAG_SUCCESS.equals(result)) {
            jedisClusterUtil.set(PERFIX + retryMsg.getMsgUUID(), "1", (int) (retryMsg.getDelayTryTime() * 2 / 1000));
        }
    }
}
