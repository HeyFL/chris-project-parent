
package org.chris.plugin.retry.rabbitmq;

import org.chris.plugin.retry.domain.RetryMsg;

/**
 * @author caizq
 * @date 2018/9/12
 * @since v1.0.0
 */
public interface IRetryProvider {

    /**
     * @param retryMsg
     * @return
     */
    void sendToRetryQueue(RetryMsg retryMsg);

    /**
     * 重试服务是否被激活, 是否是可用的
     *
     * @return
     */
    boolean isEnable();
}
