package com.shuttleshout.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 健康檢查控制器
 * 用於測試API可用性和CORS配置
 * 
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/health")
@Tag(name = "健康檢查", description = "系統健康檢查相關的API接口")
public class HealthController {

    /**
     * 健康檢查接口
     * 可用於測試CORS配置是否正常
     */
    @GetMapping
    @Operation(summary = "健康檢查", description = "檢查API服務是否正常運行，可用於測試CORS配置")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "ShuttleShout API is running");
        return ResponseEntity.ok(response);
    }

    /**
     * CORS測試接口
     * 專門用於測試跨域請求
     */
    @GetMapping("/cors-test")
    @Operation(summary = "CORS測試", description = "測試跨域請求配置是否正常")
    public ResponseEntity<Map<String, Object>> corsTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("cors", "enabled");
        response.put("message", "如果你能看到這個訊息，說明CORS配置正常");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}

