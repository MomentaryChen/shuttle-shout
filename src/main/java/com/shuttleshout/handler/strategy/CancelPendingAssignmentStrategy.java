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
 * 取消待確認分配策略
 * 處理取消待確認分配的請求
 * 清空場地的球員信息，讓場地恢復到空閒狀態
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class CancelPendingAssignmentStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private CourtService courtService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object courtIdObj = data.get("courtId");
            Object teamIdObj = data.get("teamId");
            
            log.info("收到取消待確認分配請求: courtId={}, teamId={}", courtIdObj, teamIdObj);
            
            // 驗證參數
            if (courtIdObj == null) {
                log.warn("取消待確認分配請求缺少必要參數: courtId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: courtId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            Long courtId = handler.convertToLong(courtIdObj);
            Long teamId = teamIdObj != null ? handler.convertToLong(teamIdObj) : null;
            
            if (courtId == null) {
                log.warn("參數類型無效: courtId={}", courtIdObj);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "參數類型無效");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 驗證場地是否存在
            Court court = courtService.getCourtById(courtId);
            if (court == null) {
                log.warn("場地不存在: courtId={}", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "場地不存在");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 檢查比賽是否已經開始（如果已經開始，不允許取消）
            if (court.getMatchStartedAt() != null) {
                log.warn("場地 {} 的比賽已經開始，不允許取消", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "比賽已經開始，不允許取消");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 清空場地的球員信息
            courtService.clearCourtPlayers(courtId);
            log.info("已清空場地 {} 的球員信息（取消待確認分配）", courtId);
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "CANCEL_PENDING_ASSIGNMENT_SUCCESS");
            response.put("courtId", courtId);
            response.put("teamId", teamId);
            response.put("message", "已取消分配，場地已清空");
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列
            if (teamId != null) {
                handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
            }
            
            log.info("取消待確認分配請求處理完成，已更新數據庫並廣播給所有客戶端");
        } catch (Exception e) {
            log.error("處理取消待確認分配請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "取消分配失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "CANCEL_PENDING_ASSIGNMENT";
    }
}

