package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.CourtService;
import lombok.extern.slf4j.Slf4j;

/**
 * 球員移除策略
 * 處理從場地移除球員的消息
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class RemovePlayerStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private CourtService courtService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object courtIdObj = data.get("courtId");
            Object positionObj = data.get("position");
            
            log.info("處理球員移除: courtId={}, position={}", courtIdObj, positionObj);
            
            // 驗證參數
            if (courtIdObj == null || positionObj == null) {
                log.warn("球員移除請求缺少必要參數");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: courtId, position");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 轉換參數類型
            Long courtId = handler.convertToLong(courtIdObj);
            Integer position = handler.convertToInteger(positionObj);
            
            if (courtId == null || position == null) {
                log.warn("參數類型轉換失敗");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "參數類型無效");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 注意：球員信息現在只存儲在 matches 表中
            // 從場地移除球員需要更新或取消正在進行的比賽
            // 這裡暫時只記錄日誌，實際的移除應該通過更新 Match 記錄來完成
            log.info("從場地 {} 的位置 {} 移除球員（球員信息存儲在 matches 表中）", courtId, position);
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "PLAYER_REMOVED");
            response.put("courtId", courtId);
            response.put("position", position);
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列
            Object teamIdObj = data.get("teamId");
            if (teamIdObj != null) {
                Long teamId = handler.convertToLong(teamIdObj);
                if (teamId != null) {
                    handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
                }
            }
            
        } catch (Exception e) {
            log.error("處理球員移除失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "球員移除失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "REMOVE_PLAYER";
    }
}

