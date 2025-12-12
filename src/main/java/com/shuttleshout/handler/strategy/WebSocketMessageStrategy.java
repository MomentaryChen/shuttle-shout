package com.shuttleshout.handler.strategy;

import java.util.Map;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket消息處理策略接口
 * 使用策略模式處理不同類型的WebSocket消息
 * 
 * @author ShuttleShout Team
 */
public interface WebSocketMessageStrategy {
    
    /**
     * 處理WebSocket消息
     * 
     * @param session WebSocket會話
     * @param data 消息數據
     */
    void handle(WebSocketSession session, Map<String, Object> data);
    
    /**
     * 獲取該策略支持的消息類型
     * 
     * @return 消息類型
     */
    String getMessageType();
}

