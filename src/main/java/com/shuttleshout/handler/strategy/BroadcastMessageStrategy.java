package com.shuttleshout.handler.strategy;

import java.util.Map;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 廣播消息策略
 * 處理需要直接廣播給所有客戶端的消息類型
 * 支持: QUEUE_UPDATE, COURT_UPDATE, PLAYER_ASSIGNED, PLAYER_REMOVED
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@RequiredArgsConstructor
public class BroadcastMessageStrategy implements WebSocketMessageStrategy {
    
    private final TeamCallingWebSocketHandler handler;
    private final String messageType;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        log.info("廣播消息: type={}", messageType);
        handler.broadcastMessage(handler.createMessage(messageType, data));
    }
    
    @Override
    public String getMessageType() {
        return messageType;
    }
}

