package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 手動加載隊列策略
 * 處理前端手動請求加載等待隊列的消息
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class LoadQueueStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object teamIdObj = data.get("teamId");
            Long teamId = handler.convertToLong(teamIdObj);
            
            if (teamId == null) {
                log.warn("加載隊列請求缺少 teamId 參數");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: teamId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            log.info("收到手動加載隊列請求: teamId={}", teamId);
            
            // 調用 handler 的方法加載並發送等待隊列
            handler.loadAndSendWaitingQueue(session, teamId);
            
        } catch (Exception e) {
            log.error("處理手動加載隊列請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "加載隊列失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "LOAD_QUEUE";
    }
}

