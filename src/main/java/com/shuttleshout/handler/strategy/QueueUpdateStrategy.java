package com.shuttleshout.handler.strategy;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 隊列更新廣播策略
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class QueueUpdateStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        log.info("廣播隊列更新消息");
        handler.broadcastMessage(handler.createMessage("QUEUE_UPDATE", data));
    }
    
    @Override
    public String getMessageType() {
        return "QUEUE_UPDATE";
    }
}

