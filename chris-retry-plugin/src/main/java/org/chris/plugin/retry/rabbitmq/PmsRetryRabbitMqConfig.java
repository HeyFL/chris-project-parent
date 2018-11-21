

package org.chris.plugin.retry.rabbitmq;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * 延迟重试消息队列
 *
 * @author caizq
 * @date 2018/9/14
 * @since v1.0.0
 */
@Configuration
@ConditionalOnExpression("'${rabbitmq.version.equal.or.larger.than.three.five.eight}'=='true'")
public class PmsRetryRabbitMqConfig {

    @Bean
    public ConnectionFactory pmsRetryConnectionFactory(
            @Value("${spring.rabbitmq.default.addresses}") String addresses,
            @Value("${spring.rabbitmq.default.username}") String username,
            @Value("${spring.rabbitmq.default.password}") String password,
            @Value("${spring.rabbitmq.default.virtual-host}") String virtualHost
    ) {
        CachingConnectionFactory connectionFactory = getConnectionFactory(addresses, username, password, virtualHost);
        connectionFactory.setRequestedHeartBeat(12);
        return connectionFactory;
    }

    @Bean
    @Scope("prototype")
    public RabbitTemplate pmsRetryRabbitTemplate(@Qualifier("pmsRetryConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        return template;
    }

    @Bean
    public RabbitAdmin pmsRetryRabbitAdmin(@Qualifier("pmsRetryConnectionFactory") ConnectionFactory connectionFactory) {
        Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        //定义队列
        Map<String, Object> map = new HashMap<>(2);
        map.put("x-dead-letter-exchange", "COMMON_RETRY_X_DEAD");
        map.put("x-dead-letter-routing-key", "COMMON_RETRY_R_DEAD");
        Queue queue = new Queue("COMMON_RETRY_Q", false, false, false, map);
        //定义延迟Exchange
        Map<String, Object> delayArg = new HashMap<>(2);
        delayArg.put("x-delayed-type", "direct");
        CustomExchange exchange = new CustomExchange("COMMON_RETRY_X", "x-delayed-message", false, false, delayArg);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("COMMON_RETRY_R").noargs());
        //定义死信队列
        Queue queueDead = new Queue("COMMON_RETRY_Q_DEAD", false, false, false);
        DirectExchange exchangeDead = new DirectExchange("COMMON_RETRY_X_DEAD", false, false);
        rabbitAdmin.declareQueue(queueDead);
        rabbitAdmin.declareExchange(exchangeDead);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queueDead).to(exchangeDead).with("COMMON_RETRY_R_DEAD"));

        return rabbitAdmin;
    }

    /**
     * 初始化RabbitMQ 连接池
     *
     * @param addresses
     * @param username
     * @param password
     * @param virtualHost
     * @return
     */
    private CachingConnectionFactory getConnectionFactory(String addresses, String username, String password, String virtualHost) {
        //这里指定localhost是因为linux下取不到默认配置
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setAddresses(addresses);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

}
