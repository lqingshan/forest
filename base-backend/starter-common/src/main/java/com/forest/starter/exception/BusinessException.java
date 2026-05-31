package com.forest.starter.exception;

/**
 * 表示返回给客户端的业务异常。
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建只包含业务提示信息的异常。
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 创建带原始异常原因的业务异常。
     */
    public static BusinessException of(String message, Throwable cause) {
        return new BusinessException(message, cause);
    }
}
