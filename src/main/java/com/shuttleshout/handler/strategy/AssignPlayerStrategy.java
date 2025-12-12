package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.CourtService;
import lombok.extern.slf4j.Slf4j;

/**
 * 球員分配策略
 * 處理球員分配到場地的消息
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class AssignPlayerStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private CourtService courtService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object courtIdObj = data.get("courtId");
            Object userIdObj = data.get("userId");
            Object positionObj = data.get("position");
            
            log.info("處理球員分配: courtId={}, userId={}, position={}", 
                    courtIdObj, userIdObj, positionObj);
            
            // 驗證參數
            if (courtIdObj == null || userIdObj == null || positionObj == null) {
                log.warn("球員分配請求缺少必要參數");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: courtId, userId, position");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 轉換參數類型
            Long courtId = handler.convertToLong(courtIdObj);
            Long userId = handler.convertToLong(userIdObj);
            Integer position = handler.convertToInteger(positionObj);
            
            if (courtId == null || userId == null || position == null) {
                log.warn("參數類型轉換失敗");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "參數類型無效");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 注意：球員信息現在只存儲在 matches 表中，不再存儲在 team_courts 表中
            // 如果場地有正在進行的比賽，需要更新比賽記錄；否則需要創建新的比賽記錄
            // 這裡暫時只記錄日誌，實際的分配應該通過創建或更新 Match 記錄來完成
            log.info("分配球員 {} 到場地 {} 的位置 {}（球員信息存儲在 matches 表中）", userId, courtId, position);
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "PLAYER_ASSIGNED");
            response.put("courtId", courtId);
            response.put("userId", userId);
            response.put("position", position);
            response.put("playerId", userId); // 兼容舊格式
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列（從團隊成員中排除所有場地上的球員）
            Object teamIdObj = data.get("teamId");
            if (teamIdObj != null) {
                Long teamId = handler.convertToLong(teamIdObj);
                if (teamId != null) {
                    handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
                }
            }
            
        } catch (Exception e) {
            log.error("處理球員分配失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "球員分配失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "ASSIGN_PLAYER";
    }
}

