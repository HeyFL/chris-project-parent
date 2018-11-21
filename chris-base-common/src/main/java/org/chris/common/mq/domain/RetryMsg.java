
package org.chris.common.mq.domain;

import org.chris.common.enums.EnumRequestType;
import lombok.Data;

/**
 * @author caizq
 * @date 2018/9/12
 * @since v1.0.0
 */
@Data
public class RetryMsg {
    /**
     * msg uuid 做幂等操作
     */
    private String msgUUID;
    /**
     * 已尝试次数
     */
    private int tryCount = 0;
    /**
     * 最大尝试次数
     */
    private int maxTry;
    /**
     * 消息内容
     */
    private String data;
    /**
     * 补偿请求路径
     */
    private String requsetUrl;
    /**
     * 延迟尝试时间 单位:ms
     */
    private long delayTryTime;

    /**
     * 请求方式 GET/POST
     */
    private EnumRequestType requestType;

    /**
     * JSON反序列化需要用到这个,自己代码不要使用这个来初始化
     */
    @Deprecated
    public RetryMsg() {
    }

    public RetryMsg(String msgUUID, int maxTry, String requsetUrl, EnumRequestType requestType, String data, long delayTryTime) {
        this.msgUUID = msgUUID;
        this.maxTry = maxTry;
        this.requsetUrl = requsetUrl;
        this.requestType = requestType;
        this.data = data;
        this.delayTryTime = delayTryTime;
    }


    public void addCount() {
        tryCount++;
    }
}
