package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.common.model.po.Player;
import com.shuttleshout.common.model.po.Queue;
import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;
import com.shuttleshout.service.PlayerService;
import com.shuttleshout.service.QueueService;
import lombok.extern.slf4j.Slf4j;

/**
 * 結束比賽策略
 * 處理結束比賽的消息
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class FinishMatchStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private CourtService courtService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private QueueService queueService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object courtIdObj = data.get("courtId");
            Object teamIdObj = data.get("teamId");
            
            log.info("收到結束比賽請求: courtId={}, teamId={}", courtIdObj, teamIdObj);
            
            // 驗證參數
            if (courtIdObj == null) {
                log.warn("結束比賽請求缺少必要參數: courtId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: courtId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            Long courtId = handler.convertToLong(courtIdObj);
            if (courtId == null) {
                log.warn("courtId 參數類型無效");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "courtId 參數類型無效");
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
            
            // 檢查場地上是否有球員（從 court 表檢查）
            boolean hasPlayers = court.getPlayer1Id() != null || 
                                 court.getPlayer2Id() != null || 
                                 court.getPlayer3Id() != null || 
                                 court.getPlayer4Id() != null;
            
            if (!hasPlayers) {
                log.warn("場地 {} 沒有球員，無需結束比賽", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "該場地沒有球員，無需結束比賽");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 獲取該場地正在進行的比賽（如果存在）
            Match match = matchService.getOngoingMatchByCourtId(courtId);
            if (match != null) {
                // 結束比賽（這會將比賽狀態設為 FINISHED，球員信息保留在 matches 表中）
                matchService.finishMatch(match.getId());
                log.info("比賽已結束: matchId={}, courtId={}", match.getId(), courtId);
            }
            
            // 從 court 或 match 對象中獲取 teamId（在清空前獲取，確保能正確處理下場球員）
            Long teamId = court.getTeamId();
            if (teamId == null && match != null) {
                teamId = match.getTeamId();
            }
            // 如果還是沒有 teamId，嘗試從請求參數中獲取
            if (teamId == null && teamIdObj != null) {
                teamId = handler.convertToLong(teamIdObj);
            }
            
            // 在清空場地前，保存下場球員的userId列表（用於重新加入等待隊列）
            List<Long> finishedPlayerUserIds = new ArrayList<>();
            if (court.getPlayer1Id() != null) {
                finishedPlayerUserIds.add(court.getPlayer1Id());
            }
            if (court.getPlayer2Id() != null) {
                finishedPlayerUserIds.add(court.getPlayer2Id());
            }
            if (court.getPlayer3Id() != null) {
                finishedPlayerUserIds.add(court.getPlayer3Id());
            }
            if (court.getPlayer4Id() != null) {
                finishedPlayerUserIds.add(court.getPlayer4Id());
            }
            
            log.info("準備清空場地 {} 的球員信息: players=[{}, {}, {}, {}]", 
                    courtId, court.getPlayer1Id(), court.getPlayer2Id(), 
                    court.getPlayer3Id(), court.getPlayer4Id());
            
            // 使用專門的方法清空球員信息，確保 null 值被正確更新到數據庫
            courtService.clearCourtPlayers(courtId);
            
            // 重新查詢驗證清空是否成功
            Court updatedCourt = courtService.getCourtById(courtId);
            if (updatedCourt.getPlayer1Id() != null || updatedCourt.getPlayer2Id() != null ||
                updatedCourt.getPlayer3Id() != null || updatedCourt.getPlayer4Id() != null) {
                log.error("清空場地 {} 的球員信息失敗，仍有球員存在: players=[{}, {}, {}, {}]", 
                        courtId, updatedCourt.getPlayer1Id(), updatedCourt.getPlayer2Id(),
                        updatedCourt.getPlayer3Id(), updatedCourt.getPlayer4Id());
            } else {
                log.info("已成功清空場地 {} 的球員信息和比賽時間（team_courts 表已更新）", courtId);
            }
            
            // 為下場的球員重新加入等待隊列
            int reQueuedCount = 0;
            if (teamId != null && !finishedPlayerUserIds.isEmpty()) {
                for (Long userId : finishedPlayerUserIds) {
                    try {
                        // 創建或獲取Player記錄（從用戶信息創建）
                        Player player = playerService.createPlayerFromUser(userId, teamId);
                        
                        // 為該Player創建WAITING狀態的Queue記錄
                        Queue queue = queueService.createQueue(player.getId(), null, Queue.QueueStatus.WAITING);
                        reQueuedCount++;
                        log.info("已將下場球員重新加入等待隊列: userId={}, playerId={}, queueId={}", 
                                userId, player.getId(), queue.getId());
                    } catch (Exception e) {
                        log.error("為下場球員 {} 重新加入等待隊列失敗", userId, e);
                        // 繼續處理其他球員，不影響整體流程
                    }
                }
                log.info("已為 {} 位下場球員重新加入等待隊列（共 {} 位）", reQueuedCount, finishedPlayerUserIds.size());
            } else {
                if (teamId == null) {
                    log.warn("無法將下場球員重新加入等待隊列：無法獲取 teamId");
                }
                if (finishedPlayerUserIds.isEmpty()) {
                    log.info("沒有下場球員需要重新加入等待隊列");
                }
            }
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "MATCH_FINISHED");
            response.put("courtId", courtId);
            response.put("teamId", teamId != null ? teamId : teamIdObj);
            response.put("reQueuedCount", reQueuedCount);
            response.put("message", String.format("比賽已結束，場地已清空，已將 %d 位球員重新加入等待隊列", reQueuedCount));
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 自動更新等待隊列（球員已放回等待隊列）
            if (teamId != null) {
                handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
                log.info("已自動更新等待隊列: teamId={}", teamId);
            } else {
                log.warn("無法更新等待隊列：無法獲取 teamId");
            }
            
            log.info("結束比賽請求處理完成，已更新數據庫並廣播給所有客戶端");
        } catch (Exception e) {
            log.error("處理結束比賽請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "結束比賽失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "FINISH_MATCH";
    }
}

