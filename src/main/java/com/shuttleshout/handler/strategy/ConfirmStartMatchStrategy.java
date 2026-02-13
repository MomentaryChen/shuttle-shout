package com.shuttleshout.handler.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.common.model.dto.QueueDTO;
import com.shuttleshout.common.model.dto.UserTeamDTO;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.common.model.po.Player;
import com.shuttleshout.common.model.po.Queue;
import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.repository.PlayerRepository;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;
import com.shuttleshout.service.PlayerService;
import com.shuttleshout.service.QueueService;
import com.shuttleshout.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;

/**
 * 確認開始比賽策略
 * 處理確認開始比賽的消息
 * 當用戶確認開始比賽時，會創建Match記錄、設置比賽開始時間、更新Queue狀態為SERVED
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class ConfirmStartMatchStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private CourtService courtService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private UserTeamService userTeamService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private QueueService queueService;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object courtIdObj = data.get("courtId");
            Object teamIdObj = data.get("teamId");
            
            log.info("收到確認開始比賽請求: courtId={}, teamId={}", courtIdObj, teamIdObj);
            
            // 驗證參數
            if (courtIdObj == null || teamIdObj == null) {
                log.warn("確認開始比賽請求缺少必要參數: courtId 或 teamId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: courtId 和 teamId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            Long courtId = handler.convertToLong(courtIdObj);
            Long teamId = handler.convertToLong(teamIdObj);
            
            if (courtId == null || teamId == null) {
                log.warn("參數類型無效: courtId={}, teamId={}", courtId, teamId);
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
            
            // 先從請求中獲取最終確認的人員信息（如果有），再與場地資料同步
            Long player1Id = null;
            Long player2Id = null;
            Long player3Id = null;
            Long player4Id = null;
            
            Object playersObj = data.get("players");
            if (playersObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> players = (List<Map<String, Object>>) playersObj;
                for (Map<String, Object> p : players) {
                    Long userId = handler.convertToLong(p.get("userId"));
                    Integer position = handler.convertToInteger(p.get("position"));
                    if (userId == null || position == null) {
                        continue;
                    }
                    switch (position) {
                        case 1:
                            player1Id = userId;
                            break;
                        case 2:
                            player2Id = userId;
                            break;
                        case 3:
                            player3Id = userId;
                            break;
                        case 4:
                            player4Id = userId;
                            break;
                        default:
                            break;
                    }
                }
                
                // 用最終確認的名單覆蓋 court 表上的球員資料
                court.setPlayer1Id(player1Id);
                court.setPlayer2Id(player2Id);
                court.setPlayer3Id(player3Id);
                court.setPlayer4Id(player4Id);
                court.setUpdatedAt(LocalDateTime.now());
                courtService.updateCourt(court);
                log.info("已使用確認名單更新場地 {} 的球員信息: players=[{}, {}, {}, {}]", 
                        courtId, player1Id, player2Id, player3Id, player4Id);
            } else {
                // 如果前端沒有傳 players，則使用目前場地上的球員
                player1Id = court.getPlayer1Id();
                player2Id = court.getPlayer2Id();
                player3Id = court.getPlayer3Id();
                player4Id = court.getPlayer4Id();
            }
            
            // 檢查是否有足夠的球員（至少需要4人）
            int playerCount = 0;
            if (player1Id != null) playerCount++;
            if (player2Id != null) playerCount++;
            if (player3Id != null) playerCount++;
            if (player4Id != null) playerCount++;
            
            if (playerCount < 4) {
                log.warn("場地 {} 的球員不足4人，無法開始比賽（當前: {} 人）", courtId, playerCount);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", String.format("場地球員不足4人，無法開始比賽（當前: %d 人）", playerCount));
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 檢查比賽是否已經開始
            if (court.getMatchStartedAt() != null) {
                log.warn("場地 {} 的比賽已經開始", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "比賽已經開始");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 設置比賽開始時間
            LocalDateTime now = LocalDateTime.now();
            court.setMatchStartedAt(now);
            court.setMatchEndedAt(null);
            courtService.updateCourt(court);
            log.info("已設置場地 {} 的比賽開始時間: {}", courtId, now);
            
            // 獲取團隊成員列表（用於匹配User）
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            if (teamMembers == null) {
                teamMembers = new ArrayList<>();
            }
            
            // 獲取等待隊列，用於更新Queue狀態
            List<QueueDTO> waitingQueues = queueService.getQueuesByTeamIdAndStatus(teamId, Queue.QueueStatus.WAITING);
            Map<Long, QueueDTO> queueMap = new HashMap<>();
            if (waitingQueues != null) {
                for (QueueDTO queue : waitingQueues) {
                    Player player = playerRepository.selectOneById(queue.getPlayerId());
                    if (player != null) {
                        // 通過Player的name匹配userId
                        String playerName = player.getName();
                        for (UserTeamDTO member : teamMembers) {
                            if ((playerName.equals(member.getUserRealName()) || playerName.equals(member.getUserName()))
                                    && ((player1Id != null && player1Id.equals(member.getUserId())) 
                                        || (player2Id != null && player2Id.equals(member.getUserId()))
                                        || (player3Id != null && player3Id.equals(member.getUserId())) 
                                        || (player4Id != null && player4Id.equals(member.getUserId())))) {
                                queueMap.put(member.getUserId(), queue);
                                break;
                            }
                        }
                    }
                }
            }
            
            // 為每個分配的選手更新Queue狀態為SERVED
            List<Long> assignedPlayerIds = new ArrayList<>();
            if (player1Id != null) assignedPlayerIds.add(player1Id);
            if (player2Id != null) assignedPlayerIds.add(player2Id);
            if (player3Id != null) assignedPlayerIds.add(player3Id);
            if (player4Id != null) assignedPlayerIds.add(player4Id);
            
            for (Long userId : assignedPlayerIds) {
                try {
                    // 確保Player記錄存在
                    Player player = playerService.createPlayerFromUser(userId, teamId);
                    log.info("確保球員記錄存在: playerId={}, userId={}, teamId={}", 
                            player.getId(), userId, teamId);
                    
                    // 如果該用戶有WAITING狀態的Queue，更新為SERVED
                    QueueDTO existingQueue = queueMap.get(userId);
                    if (existingQueue != null) {
                        // 更新現有Queue的狀態為SERVED
                        queueService.updateQueueStatus(existingQueue.getId(), Queue.QueueStatus.SERVED);
                        log.info("已更新隊列記錄: queueId={}, playerId={}, status=SERVED", 
                                existingQueue.getId(), player.getId());
                    } else {
                        // 創建新的Queue記錄，狀態為SERVED（已上場）
                        Queue queue = queueService.createQueue(player.getId(), courtId, Queue.QueueStatus.SERVED);
                        log.info("成功創建隊列記錄: queueId={}, playerId={}, courtId={}, status=SERVED", 
                                queue.getId(), player.getId(), courtId);
                    }
                } catch (Exception e) {
                    log.error("為用戶 {} 更新Player或Queue記錄失敗", userId, e);
                    // 不影響主流程，只記錄錯誤
                }
            }
            
            // 創建比賽記錄保存到數據庫
            try {
                Match match = matchService.createMatch(teamId, courtId, player1Id, player2Id, player3Id, player4Id);
                log.info("成功創建比賽記錄: matchId={}, teamId={}, courtId={}", 
                        match.getId(), teamId, courtId);
            } catch (Exception e) {
                log.error("創建比賽記錄失敗", e);
                // 不影響主流程，只記錄錯誤
            }
            
            // 構建響應消息
            Map<Long, UserTeamDTO> memberMap = teamMembers.stream()
                    .collect(Collectors.toMap(UserTeamDTO::getUserId, member -> member));
            
            List<Map<String, Object>> assignments = new ArrayList<>();
            if (player1Id != null) {
                Map<String, Object> assignment1 = buildAssignment(player1Id, 1, memberMap);
                assignments.add(assignment1);
            }
            if (player2Id != null) {
                Map<String, Object> assignment2 = buildAssignment(player2Id, 2, memberMap);
                assignments.add(assignment2);
            }
            if (player3Id != null) {
                Map<String, Object> assignment3 = buildAssignment(player3Id, 3, memberMap);
                assignments.add(assignment3);
            }
            if (player4Id != null) {
                Map<String, Object> assignment4 = buildAssignment(player4Id, 4, memberMap);
                assignments.add(assignment4);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "CONFIRM_START_MATCH_SUCCESS");
            response.put("courtId", courtId);
            response.put("teamId", teamId);
            response.put("assignments", assignments);
            response.put("matchStartedAt", now.toString());
            response.put("message", "比賽已開始");
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列
            handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
            
            log.info("確認開始比賽請求處理完成，已更新數據庫並廣播給所有客戶端");
        } catch (Exception e) {
            log.error("處理確認開始比賽請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "確認開始比賽失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "CONFIRM_START_MATCH";
    }
    
    /**
     * 構建分配信息，包含用戶的完整信息
     * 
     * @param userId 用戶ID
     * @param position 位置 (1-4)
     * @param memberMap 成員映射表
     * @return 分配信息Map
     */
    private Map<String, Object> buildAssignment(Long userId, int position, Map<Long, UserTeamDTO> memberMap) {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("userId", userId);
        assignment.put("position", position);
        
        // 添加用戶的詳細信息
        UserTeamDTO member = memberMap.get(userId);
        if (member != null) {
            assignment.put("userName", member.getUserName());
            assignment.put("userRealName", member.getUserRealName());
            assignment.put("userEmail", member.getUserEmail());
        }
        
        return assignment;
    }
}

