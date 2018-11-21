package org.chris.plugin.retry.rabbitmq;

import org.chris.plugin.retry.domain.RetryMsg;

/**
 * @author caizq
 * @date 2018/9/13
 * @since v1.0.0
 */
public interface IRetryRequestClient {
    /**
     * @param retryMsg
     */
    String reqeust(RetryMsg retryMsg);
}
