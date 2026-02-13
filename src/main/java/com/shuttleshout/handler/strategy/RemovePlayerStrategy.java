package com.shuttleshout.handler.strategy;

import java.time.LocalDateTime;
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
 * 球員移除策略
 * 處理從場地移除球員的消息
 * 支持通過 playerId（userId）移除球員，會自動找到對應的位置並更新 court 表
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
            Object playerIdObj = data.get("playerId");
            Object positionObj = data.get("position");
            
            log.info("處理球員移除: courtId={}, playerId={}, position={}", courtIdObj, playerIdObj, positionObj);
            
            // 驗證參數
            if (courtIdObj == null) {
                log.warn("球員移除請求缺少必要參數: courtId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: courtId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 轉換參數類型
            Long courtId = handler.convertToLong(courtIdObj);
            Long playerId = playerIdObj != null ? handler.convertToLong(playerIdObj) : null;
            Integer position = positionObj != null ? handler.convertToInteger(positionObj) : null;
            
            if (courtId == null) {
                log.warn("參數類型轉換失敗: courtId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "參數類型無效: courtId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 獲取場地信息
            Court court = courtService.getCourtById(courtId);
            if (court == null) {
                log.warn("場地不存在: courtId={}", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "場地不存在");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 如果提供了 position，直接使用；否則根據 playerId 查找位置
            Integer targetPosition = position;
            if (targetPosition == null && playerId != null) {
                // 根據 playerId 查找對應的位置
                if (playerId.equals(court.getPlayer1Id())) {
                    targetPosition = 1;
                } else if (playerId.equals(court.getPlayer2Id())) {
                    targetPosition = 2;
                } else if (playerId.equals(court.getPlayer3Id())) {
                    targetPosition = 3;
                } else if (playerId.equals(court.getPlayer4Id())) {
                    targetPosition = 4;
                } else {
                    log.warn("場地 {} 上找不到球員 {}，無法移除", courtId, playerId);
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("message", "場地上找不到該球員");
                    handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                    return;
                }
            }
            
            if (targetPosition == null) {
                log.warn("無法確定要移除的位置: courtId={}, playerId={}, position={}", courtId, playerId, position);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "無法確定要移除的位置，請提供 playerId 或 position");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 檢查比賽是否已經開始（如果已經開始，不允許移除）
            // 只有在待確認狀態下（matchStartedAt 為 null）才允許移除
            if (court.getMatchStartedAt() != null) {
                log.warn("場地 {} 的比賽已經開始，不允許移除球員", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "比賽已經開始，不允許移除球員");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 更新 court 表，清空對應位置的球員
            switch (targetPosition) {
                case 1:
                    court.setPlayer1Id(null);
                    break;
                case 2:
                    court.setPlayer2Id(null);
                    break;
                case 3:
                    court.setPlayer3Id(null);
                    break;
                case 4:
                    court.setPlayer4Id(null);
                    break;
                default:
                    log.warn("無效的位置: position={}", targetPosition);
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("message", "無效的位置，必須在 1-4 之間");
                    handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                    return;
            }
            
            court.setUpdatedAt(LocalDateTime.now());
            courtService.updateCourt(court);
            
            log.info("已從場地 {} 的位置 {} 移除球員（已更新 team_courts 表）", courtId, targetPosition);
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "PLAYER_REMOVED");
            response.put("courtId", courtId);
            response.put("playerId", playerId);
            response.put("position", targetPosition);
            
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

