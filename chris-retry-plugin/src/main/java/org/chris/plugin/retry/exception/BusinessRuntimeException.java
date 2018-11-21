
package org.chris.plugin.retry.exception;

import lombok.Getter;
import lombok.ToString;
import org.chris.plugin.retry.enums.EnumCommomSysErrorCode;

/**
 * 业务异常类
 * 18.8.30修改: BusinessRuntimeException 改为 RuntimeException By chris
 * 之所以改成RuntimeException,是因为:
 * 1.如果是继承Exception,会导致接口老是throws,破坏了迪米特原则,在同个系统throws还好,但跨系统就会导致别人系统不可用、或者服务提供者服务不可用的问题(调用失败)
 * 2.而且,不管是什么Exception,你最终都需要在最上层catch(Exception e),所以不会有错漏异常
 *
 * @author caizq
 * @date 2018/4/26
 * @since v1.0.0
 */
@Getter
@ToString
public class BusinessRuntimeException extends RuntimeException {


    /**
     * 序列号
     */
    private static final long serialVersionUID = 746264109957384560L;

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
    public BusinessRuntimeException(String errCode, String errMsg) {
        super();
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    /**
     * 构造函数
     *
     * @param sysErrorCode
     */
    public BusinessRuntimeException(EnumCommomSysErrorCode sysErrorCode) {
        super();
        this.errCode = sysErrorCode.getValue();
        this.errMsg = sysErrorCode.getDesc();
    }

    /**
     * 构造函数
     *
     * @param sysErrorCode
     */
    public BusinessRuntimeException(EnumCommomSysErrorCode sysErrorCode, Object obj) {
        super();
        this.errCode = sysErrorCode.getValue();
        this.errMsg = sysErrorCode.getDesc();
        this.obj = obj;
    }

    public BusinessRuntimeException(String errCode) {
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
    public BusinessRuntimeException(String errCode, String errMsg, Object obj) {
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
    public BusinessRuntimeException(EnumCommomSysErrorCode sysErrorCode, Throwable throwable) {
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
    public BusinessRuntimeException(String errCode, String errMsg, Throwable throwable) {
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
    public BusinessRuntimeException(String errCode, String errMsg, Object obj, Throwable throwable) {
        super(errMsg, throwable);
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.obj = obj;
    }


    public static  BusinessRuntimeException buildBusyException(EnumCommomSysErrorCode sysErrorCode, Throwable throwable) {
        return new BusinessRuntimeException(sysErrorCode.getValue(), sysErrorCode.getDesc(), throwable);
    }

    public static  BusinessRuntimeException buildBusyException(String errCode, String errMsg, Throwable throwable) {
        return new BusinessRuntimeException(errCode, errMsg, throwable);
    }

    public static  BusinessRuntimeException buildBusyException(String errCode, String errMsg, Object obj, Throwable throwable) {
        return new BusinessRuntimeException(errCode, errMsg, obj, throwable);
    }


    public static BusinessRuntimeException buildBusyException() {
        return new BusinessRuntimeException(EnumCommomSysErrorCode.SERVICE_BUSY_ERROR.getValue(), EnumCommomSysErrorCode.SERVICE_BUSY_ERROR.getDesc());
    }

    /**
     * @param errMsg
     * @return
     * @Deprecated 建议使用入参为EnumCommomSysErrorCode的
     */
    @Deprecated
    public static BusinessRuntimeException buildBusyException(String errMsg) {
        return new BusinessRuntimeException(EnumCommomSysErrorCode.SERVICE_BUSY_ERROR.getValue(), errMsg);
    }

    public static BusinessRuntimeException buildBusyException(EnumCommomSysErrorCode sysErrorCode) {
        return new BusinessRuntimeException(sysErrorCode.getValue(), sysErrorCode.getDesc());
    }

    public static BusinessRuntimeException buildBusyException(EnumCommomSysErrorCode sysErrorCode, String desc) {
        return new BusinessRuntimeException(sysErrorCode.getValue(), desc);
    }

    /**
     * @param errCode
     * @param errMsg
     * @param obj
     * @return
     * @Deprecated 建议使用入参为EnumCommomSysErrorCode的
     */
    @Deprecated
    public static BusinessRuntimeException buildBusyException(String errCode, String errMsg, Object obj) {
        return new BusinessRuntimeException(errCode, errMsg, obj);
    }


    /**
     * 这个未知异常不好,最好用 buildUnknownException(Throwable throwable)
     * 因为这个报错后 你完全不知道是什么导致的
     *
     * @return
     */
    @Deprecated
    public static BusinessRuntimeException buildUnknownException() {
        return new BusinessRuntimeException(EnumCommomSysErrorCode.UNKNOW_ERROR.getValue(), EnumCommomSysErrorCode.UNKNOW_ERROR.getDesc());
    }

    public static BusinessRuntimeException buildUnknownException(Throwable throwable) {
        return new BusinessRuntimeException(EnumCommomSysErrorCode.UNKNOW_ERROR.getValue(), EnumCommomSysErrorCode.UNKNOW_ERROR.getDesc(), throwable);
    }

    public static boolean exceptionEquals(Exception e, EnumCommomSysErrorCode errorCode) {
        if (e instanceof BusinessRuntimeException && ((BusinessRuntimeException) e).errCode == errorCode.getValue()) {
            return true;
        }
        return false;
    }
}



