package com.shuttleshout.common.exception;

import org.springframework.http.HttpStatus;

/**
 * API异常类
 * 用于统一处理API层的异常
 * 
 * @author ShuttleShout Team
 */
public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus httpStatus;
    private final String errorCode;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public ApiException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_ERROR";
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param httpStatus HTTP状态码
     */
    public ApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = "API_ERROR";
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param httpStatus HTTP状态码
     * @param errorCode 错误代码
     */
    public ApiException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_ERROR";
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param httpStatus HTTP状态码
     * @param cause 原因异常
     */
    public ApiException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = "API_ERROR";
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param httpStatus HTTP状态码
     * @param errorCode 错误代码
     * @param cause 原因异常
     */
    public ApiException(String message, HttpStatus httpStatus, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    /**
     * 构造函数 - 使用 ErrorCode Enum
     * 
     * @param errorCode 错误代码枚举
     */
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }

    /**
     * 构造函数 - 使用 ErrorCode Enum 和自定义消息
     * 
     * @param errorCode 错误代码枚举
     * @param message 自定义异常消息
     */
    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }

    /**
     * 构造函数 - 使用 ErrorCode Enum 和原因异常
     * 
     * @param errorCode 错误代码枚举
     * @param cause 原因异常
     */
    public ApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }

    /**
     * 构造函数 - 使用 ErrorCode Enum、自定义消息和原因异常
     * 
     * @param errorCode 错误代码枚举
     * @param message 自定义异常消息
     * @param cause 原因异常
     */
    public ApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCode = errorCode.getCode();
    }

    /**
     * 获取HTTP状态码
     * 
     * @return HTTP状态码
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
}

