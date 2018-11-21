
package org.chris.common.response;

import lombok.Data;

/**
 * @author caizq
 * @date 2018/10/13
 * @since v1.0.0
 */
@Data
public class ErrorMsg {
    /**
     * 异常字段
     */
    private String root;
    /**
     * 异常原因
     */
    private String reason;

    public ErrorMsg() {
    }

    public ErrorMsg(String root, String reason) {
        this.root = root;
        this.reason = reason;
    }

    public static ErrorMsg buildErrorMsg(String root, String reason) {
        return new ErrorMsg(root, reason);
    }
}
