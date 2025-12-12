package com.shuttleshout.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置類
 * 配置WebSocket端點和消息代理
 * 
 * @author ShuttleShout Team
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 啟用簡單的內存消息代理，用於向客戶端發送消息
        // 前綴為 /topic 的消息將被路由到消息代理
        config.enableSimpleBroker("/topic", "/queue");
        // 設置應用程序前綴，客戶端發送消息時需要使用此前綴
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 註冊WebSocket端點，客戶端可以連接到此端點
        // 允許跨域訪問
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // 同時支持原生WebSocket連接（不使用SockJS）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
