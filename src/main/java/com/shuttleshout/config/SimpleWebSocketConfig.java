package com.shuttleshout.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;

import lombok.RequiredArgsConstructor;

/**
 * 簡單WebSocket配置類
 * 用於支持原生WebSocket連接（不使用STOMP）
 * 
 * @author ShuttleShout Team
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class SimpleWebSocketConfig implements WebSocketConfigurer {

    private final TeamCallingWebSocketHandler teamCallingWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 註冊WebSocket處理器，支持跨域
        registry.addHandler(teamCallingWebSocketHandler, "/ws")
                .setAllowedOriginPatterns("*");
    }
}
