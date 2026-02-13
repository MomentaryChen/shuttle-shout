package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.QueueService;
import lombok.extern.slf4j.Slf4j;

/**
 * 清除隊列策略
 * 刪除團隊的所有WAITING狀態的隊列
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class ClearQueueStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private QueueService queueService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object teamIdObj = data.get("teamId");
            
            log.info("收到清除隊列請求: teamId={}", teamIdObj);
            
            // 驗證參數
            if (teamIdObj == null) {
                log.warn("清除隊列請求缺少必要參數: teamId");
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
            
            // 刪除團隊的所有WAITING狀態的隊列
            int deletedCount = queueService.deleteWaitingQueuesByTeamId(teamId);
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "CLEAR_QUEUE_SUCCESS");
            response.put("teamId", teamId);
            response.put("deletedCount", deletedCount);
            response.put("message", String.format("已刪除 %d 個等待隊列", deletedCount));
            
            // 發送給當前會話
            handler.sendMessage(session, response);
            
            // 廣播給所有客戶端，更新等待隊列
            handler.sendWaitingQueueUpdate(null, teamId, new java.util.ArrayList<>());
            
            log.info("清除隊列請求處理完成: teamId={}, deletedCount={}", teamId, deletedCount);
        } catch (Exception e) {
            log.error("處理清除隊列請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "清除隊列失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "CLEAR_QUEUE";
    }
}

