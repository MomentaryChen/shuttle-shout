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
 * 恢復狀態策略
 * 恢復上次進行中的比賽狀態
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class RestoreStateStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object teamIdObj = data.get("teamId");
            
            log.info("收到恢復狀態請求: teamId={}", teamIdObj);
            
            // 驗證參數
            if (teamIdObj == null) {
                log.warn("恢復狀態請求缺少必要參數: teamId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: teamId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            Long teamId = handler.convertToLong(teamIdObj);
            
            if (teamId == null) {
                log.warn("參數類型無效: teamId={}", teamIdObj);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "參數類型無效");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 調用handler的方法恢復場上比賽狀態
            handler.loadAndSendOngoingMatches(session, teamId);
            
            // 同時恢復等待隊列（從queues表中查詢WAITING狀態的記錄）
            handler.loadAndSendWaitingQueue(session, teamId);
            
            log.info("恢復狀態請求處理完成: teamId={}（已恢復進行中的比賽和等待隊列）", teamId);
        } catch (Exception e) {
            log.error("處理恢復狀態請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "恢復狀態失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "RESTORE_STATE";
    }
}

