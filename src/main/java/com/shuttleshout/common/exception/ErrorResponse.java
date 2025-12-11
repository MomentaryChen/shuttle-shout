package com.shuttleshout.common.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误响应DTO
 * 用于统一返回错误信息
 * 
 * @author ShuttleShout Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String message;

    /**
     * HTTP状态码
     */
    private int status;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求路径（可选）
     */
    private String path;
}

