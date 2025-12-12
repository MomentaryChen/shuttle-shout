package com.shuttleshout.handler.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.common.model.dto.UserTeamDTO;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;
import com.shuttleshout.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;

/**
 * 自動分配策略
 * 處理從隊列自動分配人員到場地的消息
 * 後端自動從團隊成員中選擇4位成員進行分配
 * 當確認開始比賽時，會創建Match記錄保存到數據庫
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class AutoAssignStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private CourtService courtService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private UserTeamService userTeamService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object courtIdObj = data.get("courtId");
            Object teamIdObj = data.get("teamId");
            
            log.info("收到自動分配請求: courtId={}, teamId={}", courtIdObj, teamIdObj);
            
            // 驗證參數
            if (courtIdObj == null || teamIdObj == null) {
                log.warn("自動分配請求缺少必要參數: courtId 或 teamId");
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
            
            // 驗證場地是否存在（如果不存在會拋出異常）
            courtService.getCourtById(courtId);
            
            // 獲取團隊成員列表
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            if (teamMembers == null || teamMembers.isEmpty()) {
                log.warn("團隊 {} 沒有成員", teamId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "團隊沒有可分配的成員");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 獲取團隊的所有場地
            List<Court> courts = courtService.getCourtsByTeamId(teamId);
            
            // 收集所有在場地上的球員ID（從 matches 表中獲取）
            // 這包括所有場地，而不僅僅是當前場地，避免重複分配已在其他場地的球員
            Set<Long> allPlayersOnCourt = new HashSet<>();
            if (courts != null) {
                for (Court c : courts) {
                    List<Long> players = courtService.getPlayersOnCourt(c.getId());
                    for (Long playerId : players) {
                        if (playerId != null) {
                            allPlayersOnCourt.add(playerId);
                        }
                    }
                }
            }
            
            // 獲取當前場地上已有的球員ID列表（用於計算需要分配的人數）
            List<Long> currentCourtPlayerIds = new ArrayList<>();
            List<Long> playersOnCurrentCourt = courtService.getPlayersOnCourt(courtId);
            for (Long playerId : playersOnCurrentCourt) {
                if (playerId != null) {
                    currentCourtPlayerIds.add(playerId);
                }
            }
            
            // 從團隊成員中過濾出不在任何場地上的成員
            List<UserTeamDTO> availableMembers = teamMembers.stream()
                    .filter(member -> !allPlayersOnCourt.contains(member.getUserId()))
                    .collect(Collectors.toList());
            
            // 計算需要分配的人數（補滿4人，只考慮當前場地）
            int neededPlayers = 4 - currentCourtPlayerIds.size();
            
            if (availableMembers.size() < neededPlayers) {
                log.warn("可用成員不足: 需要 {} 人，但只有 {} 人可用", neededPlayers, availableMembers.size());
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", String.format("可用成員不足：需要 %d 人，但只有 %d 人可用", neededPlayers, availableMembers.size()));
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 選擇前 neededPlayers 位成員
            List<UserTeamDTO> selectedMembers = availableMembers.subList(0, neededPlayers);
            
            // 獲取場地上已有的球員位置（從 court 表中獲取）
            Court currentCourt = courtService.getCourtById(courtId);
            Long player1Id = currentCourt != null ? currentCourt.getPlayer1Id() : null;
            Long player2Id = currentCourt != null ? currentCourt.getPlayer2Id() : null;
            Long player3Id = currentCourt != null ? currentCourt.getPlayer3Id() : null;
            Long player4Id = currentCourt != null ? currentCourt.getPlayer4Id() : null;
            
            // 為新選中的成員分配位置（補滿4人）
            for (UserTeamDTO member : selectedMembers) {
                // 找到第一個空位置
                if (player1Id == null) {
                    player1Id = member.getUserId();
                } else if (player2Id == null) {
                    player2Id = member.getUserId();
                } else if (player3Id == null) {
                    player3Id = member.getUserId();
                } else if (player4Id == null) {
                    player4Id = member.getUserId();
                }
            }
            
            // 更新 court 表上的球員信息和比賽開始時間
            try {
                Court court = courtService.getCourtById(courtId);
                if (court != null) {
                    court.setPlayer1Id(player1Id);
                    court.setPlayer2Id(player2Id);
                    court.setPlayer3Id(player3Id);
                    court.setPlayer4Id(player4Id);
                    court.setMatchStartedAt(LocalDateTime.now());
                    court.setMatchEndedAt(null);
                    courtService.updateCourt(court);
                    log.info("已更新場地 {} 的球員信息: players=[{}, {}, {}, {}]", 
                            courtId, player1Id, player2Id, player3Id, player4Id);
                }
            } catch (Exception e) {
                log.error("更新場地球員信息失敗", e);
                // 不影響主流程，只記錄錯誤
            }
            
            log.info("準備為場地 {} 分配球員: players=[{}, {}, {}, {}]", 
                    courtId, player1Id, player2Id, player3Id, player4Id);
            
            // 創建比賽記錄保存到數據庫
            try {
                Match match = matchService.createMatch(teamId, courtId, player1Id, player2Id, player3Id, player4Id);
                log.info("成功創建比賽記錄: matchId={}, teamId={}, courtId={}", 
                        match.getId(), teamId, courtId);
            } catch (Exception e) {
                log.error("創建比賽記錄失敗", e);
                // 不影響主流程，只記錄錯誤
            }
            
            // 構建響應消息，包含完整的分配信息（用於前端更新）
            // 創建一個映射，方便根據userId查找用戶信息
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
            response.put("type", "AUTO_ASSIGN_SUCCESS");
            response.put("courtId", courtId);
            response.put("teamId", teamId);
            response.put("assignments", assignments);
            response.put("message", "自動分配成功，比賽已開始");
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列
            handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
            
            log.info("自動分配請求處理完成，已更新數據庫並廣播給所有客戶端");
        } catch (Exception e) {
            log.error("處理自動分配請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "自動分配失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "AUTO_ASSIGN";
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

