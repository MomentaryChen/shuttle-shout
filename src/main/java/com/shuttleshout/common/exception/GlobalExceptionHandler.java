package com.shuttleshout.common.exception;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 * 统一处理所有Controller抛出的异常
 * 
 * @author ShuttleShout Team
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理ApiException异常
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .status(ex.getHttpStatus().value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * 处理参数验证异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("参数验证失败: {}", ex.getMessage(), ex);
        
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message(errorMessage.toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        log.error("绑定异常: {}", ex.getMessage(), ex);
        
        StringBuilder errorMessage = new StringBuilder("参数绑定失败: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("BIND_ERROR")
                .message(errorMessage.toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        log.error("约束违反异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("CONSTRAINT_VIOLATION")
                .message("参数约束违反: " + ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex, HttpServletRequest request) {
        log.error("认证异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("AUTHENTICATION_ERROR")
                .message("认证失败: " + ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.error("权限不足: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ACCESS_DENIED")
                .message("权限不足: " + ex.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 处理IllegalStateException异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        log.error("非法状态异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ILLEGAL_STATE")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理IllegalArgumentException异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.error("非法参数异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ILLEGAL_ARGUMENT")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 处理RuntimeException异常（通用运行时异常）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        log.error("运行时异常: {}", ex.getMessage(), ex);
        
        // 将RuntimeException包装成ApiException
        ApiException apiException = new ApiException(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "RUNTIME_ERROR",
                ex
        );
        
        return handleApiException(apiException, request);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("未处理的异常: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("服务器内部错误: " + ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

