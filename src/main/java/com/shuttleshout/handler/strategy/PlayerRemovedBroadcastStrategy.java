package com.shuttleshout.handler.strategy;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 球員移除廣播策略
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class PlayerRemovedBroadcastStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        log.info("廣播球員移除消息");
        handler.broadcastMessage(handler.createMessage("PLAYER_REMOVED", data));
    }
    
    @Override
    public String getMessageType() {
        return "PLAYER_REMOVED";
    }
}

