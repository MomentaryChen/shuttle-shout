package com.shuttleshout.handler.strategy;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            
            // 驗證位置範圍
            if (position < 1 || position > 4) {
                log.warn("位置參數無效: position={}", position);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "位置參數無效，必須在 1-4 之間");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 獲取 teamId（用於檢查所有場地）
            Object teamIdObj = data.get("teamId");
            Long teamId = teamIdObj != null ? handler.convertToLong(teamIdObj) : null;
            
            // 通過 team_courts 表檢查該球員是否已經在任何場地上（去重）
            if (teamId != null) {
                List<Court> courts = courtService.getCourtsByTeamId(teamId);
                Set<Long> allPlayersOnCourt = new HashSet<>();
                if (courts != null) {
                    for (Court court : courts) {
                        List<Long> players = courtService.getPlayersOnCourt(court.getId());
                        for (Long playerId : players) {
                            if (playerId != null) {
                                allPlayersOnCourt.add(playerId);
                            }
                        }
                    }
                }
                
                // 檢查該球員是否已經在其他場地上
                if (allPlayersOnCourt.contains(userId)) {
                    log.warn("球員 {} 已經在其他場地上，無法重複分配", userId);
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("message", "該球員已經在其他場地上，無法重複分配");
                    handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                    return;
                }
            }
            
            // 獲取當前場地信息
            Court court = courtService.getCourtById(courtId);
            if (court == null) {
                log.warn("場地不存在: courtId={}", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "場地不存在");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 檢查該位置是否已被占用
            Long existingPlayerId = null;
            switch (position) {
                case 1:
                    existingPlayerId = court.getPlayer1Id();
                    break;
                case 2:
                    existingPlayerId = court.getPlayer2Id();
                    break;
                case 3:
                    existingPlayerId = court.getPlayer3Id();
                    break;
                case 4:
                    existingPlayerId = court.getPlayer4Id();
                    break;
            }
            
            // 如果該位置已有其他球員，先清空（允許替換）
            if (existingPlayerId != null && !existingPlayerId.equals(userId)) {
                log.info("位置 {} 已有球員 {}，將被替換為 {}", position, existingPlayerId, userId);
            }
            
            // 更新 court 表的球員信息
            switch (position) {
                case 1:
                    court.setPlayer1Id(userId);
                    break;
                case 2:
                    court.setPlayer2Id(userId);
                    break;
                case 3:
                    court.setPlayer3Id(userId);
                    break;
                case 4:
                    court.setPlayer4Id(userId);
                    break;
            }
            
            // 如果這是第一個球員，設置比賽開始時間
            if (court.getMatchStartedAt() == null && 
                (court.getPlayer1Id() != null || court.getPlayer2Id() != null || 
                 court.getPlayer3Id() != null || court.getPlayer4Id() != null)) {
                court.setMatchStartedAt(LocalDateTime.now());
            }
            
            court.setUpdatedAt(LocalDateTime.now());
            courtService.updateCourt(court);
            
            log.info("已分配球員 {} 到場地 {} 的位置 {}（已更新 team_courts 表）", userId, courtId, position);
            
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
            if (teamId != null) {
                handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
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

