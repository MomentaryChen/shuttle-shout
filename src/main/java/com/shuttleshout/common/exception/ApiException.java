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

