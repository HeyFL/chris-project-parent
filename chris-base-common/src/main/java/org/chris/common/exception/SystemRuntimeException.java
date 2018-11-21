
package org.chris.common.exception;

import lombok.Getter;
import org.chris.common.enums.EnumCommomSysErrorCode;

/**
 * 系统异常类
 *
 * @author caizq
 * @date 2018/4/26
 * @since v1.0.0
 */
@Getter
public class SystemRuntimeException extends RuntimeException {

    /**
     * 序列号
     */
    private static final long serialVersionUID = -2193150553300043406L;

    /**
     * 异常代码<br>
     * <p>
     * 用于定位一个或一类异常，从资源文件中查找异常信息返回给用户
     */
    private String errCode;

    /**
     * 异常消息<br>
     * <p>
     * 这个异常消息只用于输出日志，或者在API里使用，返回给用户的消息通过errCode查找资源文件获取
     */
    private String errMsg;

    /**
     * 消息参数<br>
     * <p>
     * 传递异常消息需要的参数
     */
    private Object obj;

    /**
     * 构造函数
     *
     * @param errCode 异常代码
     * @param errMsg  异常消息
     */
    public SystemRuntimeException(String errCode, String errMsg) {
        super();
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    /**
     * 构造函数
     *
     * @param sysErrorCode
     */
    public SystemRuntimeException(EnumCommomSysErrorCode sysErrorCode) {
        super();
        this.errCode = sysErrorCode.getValue();
        this.errMsg = sysErrorCode.getDesc();
    }

    /**
     * 构造函数
     *
     * @param sysErrorCode
     */
    public SystemRuntimeException(EnumCommomSysErrorCode sysErrorCode, Object obj) {
        super();
        this.errCode = sysErrorCode.getValue();
        this.errMsg = sysErrorCode.getDesc();
        this.obj = obj;
    }

    public SystemRuntimeException(String errCode) {
        super();
        this.errCode = errCode;
    }

    /**
     * 构造函数
     *
     * @param errCode 异常代码
     * @param errMsg  异常消息
     * @param obj     消息参数
     */
    public SystemRuntimeException(String errCode, String errMsg, Object obj) {
        super();
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.obj = obj;
    }


    /**
     * 方便定位问题 异常堆栈输出
     * @param sysErrorCode
     * @param throwable
     */
    public SystemRuntimeException(EnumCommomSysErrorCode sysErrorCode, Throwable throwable) {
        super(sysErrorCode.getDesc(), throwable);
        this.errCode = sysErrorCode.getValue();
        this.errMsg = sysErrorCode.getDesc();
    }

    /**
     * 方便定位问题 异常堆栈输出
     * @param errCode
     * @param errMsg
     * @param throwable
     */
    public SystemRuntimeException(String errCode, String errMsg, Throwable throwable) {
        super(errMsg, throwable);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    /**
     * 方便定位问题 异常堆栈输出
     * @param errCode
     * @param errMsg
     * @param obj
     * @param throwable
     */
    public SystemRuntimeException(String errCode, String errMsg, Object obj, Throwable throwable) {
        super(errMsg, throwable);
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.obj = obj;
    }

    public static SystemRuntimeException buildBusyException(EnumCommomSysErrorCode sysErrorCode, Throwable throwable) {
        return new SystemRuntimeException(sysErrorCode.getValue(), sysErrorCode.getDesc(), throwable);
    }

    public static SystemRuntimeException buildBusyException(String errCode, String errMsg, Throwable throwable) {
        return new SystemRuntimeException(errCode, errMsg, throwable);
    }

    public static SystemRuntimeException buildBusyException(String errCode, String errMsg, Object obj, Throwable throwable) {
        return new SystemRuntimeException(errCode, errMsg, obj, throwable);
    }


    public static SystemRuntimeException buildBusyException() {
        return new SystemRuntimeException(EnumCommomSysErrorCode.SERVICE_BUSY_ERROR.getValue(), EnumCommomSysErrorCode.SERVICE_BUSY_ERROR.getDesc());
    }
    public static SystemRuntimeException buildUnknownException(Throwable throwable) {
        return new SystemRuntimeException(EnumCommomSysErrorCode.UNKNOW_ERROR.getValue(), EnumCommomSysErrorCode.UNKNOW_ERROR.getDesc(), throwable);
    }



    /**
     * @param errMsg
     * @return
     * @Deprecated 建议使用入参为EnumCommomSysErrorCode的
     */
    @Deprecated
    public static SystemRuntimeException buildBusyException(String errMsg) {
        return new SystemRuntimeException(EnumCommomSysErrorCode.SERVICE_BUSY_ERROR.getValue(), errMsg);
    }

    public static SystemRuntimeException buildBusyException(EnumCommomSysErrorCode sysErrorCode) {
        return new SystemRuntimeException(sysErrorCode.getValue(), sysErrorCode.getDesc());
    }

    public static SystemRuntimeException buildBusyException(EnumCommomSysErrorCode sysErrorCode, String desc) {
        return new SystemRuntimeException(sysErrorCode.getValue(), desc);
    }

    /**
     * @param errCode
     * @param errMsg
     * @param obj
     * @return
     * @Deprecated 建议使用入参为EnumCommomSysErrorCode的
     */
    @Deprecated
    public static SystemRuntimeException buildBusyException(String errCode, String errMsg, Object obj) {
        return new SystemRuntimeException(errCode, errMsg, obj);
    }


    /**
     * 这个未知异常不好,最好用buildUnknownException(Object obj)
     * 因为这个报错后 你完全不知道是什么导致的
     *
     * @return
     */
    @Deprecated
    public static SystemRuntimeException buildUnknownException() {
        return new SystemRuntimeException(EnumCommomSysErrorCode.UNKNOW_ERROR.getValue(), EnumCommomSysErrorCode.UNKNOW_ERROR.getDesc());
    }
}



