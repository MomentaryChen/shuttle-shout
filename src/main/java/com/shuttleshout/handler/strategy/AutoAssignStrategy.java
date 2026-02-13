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
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private QueueService queueService;
    
    @Autowired
    private PlayerRepository playerRepository;
    
    /**
     * 分配方式枚举
     */
    public enum AssignmentMethod {
        WAITING_TIME,  // 等待时间（默认）
        RANDOM,        // 随机
        ROUND_ROBIN    // 轮换
    }
    
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
            
            // 獲取當前場地上已有的球員ID列表（用於計算需要分配的人數）
            List<Long> currentCourtPlayerIds = new ArrayList<>();
            List<Long> playersOnCurrentCourt = courtService.getPlayersOnCourt(courtId);
            for (Long playerId : playersOnCurrentCourt) {
                if (playerId != null) {
                    currentCourtPlayerIds.add(playerId);
                }
            }
            
            // 計算需要分配的人數（補滿4人，只考慮當前場地）
            int neededPlayers = 4 - currentCourtPlayerIds.size();
            
            if (neededPlayers <= 0) {
                log.warn("場地 {} 已經滿員，無需分配", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "場地已經滿員，無需分配");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 直接從queues表獲取WAITING狀態的隊列，選擇等待時間最長的成員
            List<QueueDTO> waitingQueues = queueService.getQueuesByTeamIdAndStatus(teamId, Queue.QueueStatus.WAITING);
            
            if (waitingQueues == null || waitingQueues.isEmpty()) {
                log.warn("團隊 {} 沒有等待隊列，無法分配", teamId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "沒有等待隊列，無法分配");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 獲取團隊的所有場地，收集所有在場地上的球員ID（排除已在場上的）
            List<Court> courts = courtService.getCourtsByTeamId(teamId);
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
            
            // 獲取團隊成員列表（用於匹配User）
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            if (teamMembers == null) {
                teamMembers = new ArrayList<>();
            }
            
            // 按queueNumber和createdAt排序（等待時間最長的優先）
            waitingQueues.sort((q1, q2) -> {
                // 首先按queueNumber排序（升序，號碼小的優先）
                if (q1.getQueueNumber() != null && q2.getQueueNumber() != null) {
                    int queueNumberCompare = q1.getQueueNumber().compareTo(q2.getQueueNumber());
                    if (queueNumberCompare != 0) {
                        return queueNumberCompare;
                    }
                }
                // 如果queueNumber相同或為null，按createdAt排序（升序，最早創建的優先）
                if (q1.getCreatedAt() == null && q2.getCreatedAt() == null) return 0;
                if (q1.getCreatedAt() == null) return 1;
                if (q2.getCreatedAt() == null) return -1;
                return q1.getCreatedAt().compareTo(q2.getCreatedAt());
            });
            
            // 從等待隊列中選擇成員（選擇等待時間最長的neededPlayers位）
            List<UserTeamDTO> selectedMembers = new ArrayList<>();
            Set<Long> selectedUserIds = new HashSet<>(); // 用於去重
            
            for (QueueDTO queue : waitingQueues) {
                if (selectedMembers.size() >= neededPlayers) {
                    break;
                }
                
                // 通過playerId獲取Player實體
                Player player = playerRepository.selectOneById(queue.getPlayerId());
                if (player == null) {
                    log.warn("無法找到Player: playerId={}", queue.getPlayerId());
                    continue;
                }
                
                // 通過Player的name匹配User
                String playerName = player.getName();
                UserTeamDTO matchedMember = teamMembers.stream()
                        .filter(m -> !allPlayersOnCourt.contains(m.getUserId()))
                        .filter(m -> !selectedUserIds.contains(m.getUserId())) // 避免重複選擇
                        .filter(m -> playerName.equals(m.getUserRealName()) || playerName.equals(m.getUserName()))
                        .findFirst()
                        .orElse(null);
                
                if (matchedMember != null) {
                    selectedMembers.add(matchedMember);
                    selectedUserIds.add(matchedMember.getUserId());
                    log.debug("選擇等待者: userId={}, playerName={}, queueNumber={}, createdAt={}", 
                            matchedMember.getUserId(), playerName, queue.getQueueNumber(), queue.getCreatedAt());
                } else {
                    log.warn("無法匹配等待隊列中的Player到User: playerId={}, playerName={}", 
                            queue.getPlayerId(), playerName);
                }
            }
            
            if (selectedMembers.size() < neededPlayers) {
                log.warn("可用成員不足: 需要 {} 人，但只有 {} 人可用", neededPlayers, selectedMembers.size());
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", String.format("可用成員不足：需要 %d 人，但只有 %d 人可用", neededPlayers, selectedMembers.size()));
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            log.info("從等待隊列中選擇了 {} 位成員（需要 {} 位，等待隊列總數: {}）", 
                    selectedMembers.size(), neededPlayers, waitingQueues.size());
            
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
            
            // 更新 court 表上的球員信息（不設置比賽開始時間，等待用戶確認）
            try {
                Court court = courtService.getCourtById(courtId);
                if (court != null) {
                    court.setPlayer1Id(player1Id);
                    court.setPlayer2Id(player2Id);
                    court.setPlayer3Id(player3Id);
                    court.setPlayer4Id(player4Id);
                    // 不設置 matchStartedAt，等待用戶確認後再設置
                    // court.setMatchStartedAt(LocalDateTime.now());
                    court.setMatchEndedAt(null);
                    courtService.updateCourt(court);
                    log.info("已更新場地 {} 的球員信息（待確認）: players=[{}, {}, {}, {}]", 
                            courtId, player1Id, player2Id, player3Id, player4Id);
                }
            } catch (Exception e) {
                log.error("更新場地球員信息失敗", e);
                // 不影響主流程，只記錄錯誤
            }
            
            log.info("準備為場地 {} 分配球員: players=[{}, {}, {}, {}]", 
                    courtId, player1Id, player2Id, player3Id, player4Id);
            
            // 為每個分配的選手創建Player記錄和Queue記錄
            List<Long> assignedPlayerIds = new ArrayList<>();
            if (player1Id != null) assignedPlayerIds.add(player1Id);
            if (player2Id != null) assignedPlayerIds.add(player2Id);
            if (player3Id != null) assignedPlayerIds.add(player3Id);
            if (player4Id != null) assignedPlayerIds.add(player4Id);
            
            // 獲取等待隊列，用於更新Queue狀態（重用之前獲取的waitingQueues）
            Map<Long, QueueDTO> queueMap = new HashMap<>();
            if (waitingQueues != null) {
                for (QueueDTO queue : waitingQueues) {
                    Player player = playerRepository.selectOneById(queue.getPlayerId());
                    if (player != null) {
                        // 通過Player的name匹配userId
                        String playerName = player.getName();
                        for (UserTeamDTO member : teamMembers) {
                            if ((playerName.equals(member.getUserRealName()) || playerName.equals(member.getUserName()))
                                    && assignedPlayerIds.contains(member.getUserId())) {
                                queueMap.put(member.getUserId(), queue);
                                break;
                            }
                        }
                    }
                }
            }
            
            for (Long userId : assignedPlayerIds) {
                try {
                    // 創建Player記錄（從用戶信息創建）
                    Player player = playerService.createPlayerFromUser(userId, teamId);
                    log.info("成功創建球員記錄: playerId={}, userId={}, teamId={}", 
                            player.getId(), userId, teamId);
                    
                    // 如果該用戶有WAITING狀態的Queue，保持WAITING狀態（等待確認後再更新為SERVED）
                    // 否則創建新的Queue記錄，狀態為WAITING（待確認）
                    QueueDTO existingQueue = queueMap.get(userId);
                    if (existingQueue != null) {
                        // 保持WAITING狀態，等待確認後再更新為SERVED
                        log.info("保持隊列記錄為WAITING狀態: queueId={}, playerId={}, status=WAITING（待確認）", 
                                existingQueue.getId(), player.getId());
                    } else {
                        // 創建新的Queue記錄，狀態為WAITING（待確認）
                        Queue queue = queueService.createQueue(player.getId(), courtId, Queue.QueueStatus.WAITING);
                        log.info("成功創建隊列記錄: queueId={}, playerId={}, courtId={}, status=WAITING（待確認）", 
                                queue.getId(), player.getId(), courtId);
                    }
                } catch (Exception e) {
                    log.error("為用戶 {} 創建Player或Queue記錄失敗", userId, e);
                    // 不影響主流程，只記錄錯誤
                }
            }
            
            // 不立即創建比賽記錄，等待用戶確認後再創建
            // 比賽記錄將在 ConfirmStartMatchStrategy 中創建
            
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
            
            // 獲取更新後的場地信息
            Court updatedCourt = courtService.getCourtById(courtId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "AUTO_ASSIGN_SUCCESS");
            response.put("courtId", courtId);
            response.put("teamId", teamId);
            response.put("assignments", assignments);
            response.put("isPending", true); // 標記為待確認狀態
            // 不設置 matchStartedAt，因為比賽尚未開始
            response.put("message", "自動分配成功，請確認後開始比賽");
            
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
     * 根據分配方式選擇成員
     * 
     * @param teamId 團隊ID
     * @param neededPlayers 需要的人數
     * @param method 分配方式
     * @return 選中的成員列表
     */
    private List<UserTeamDTO> selectMembersByMethod(Long teamId, int neededPlayers, AssignmentMethod method) {
        // 獲取團隊的所有場地
        List<Court> courts = courtService.getCourtsByTeamId(teamId);
        
        // 收集所有在場地上的球員ID（從 matches 表中獲取）
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
        
        List<UserTeamDTO> selectedMembers = new ArrayList<>();
        
        switch (method) {
            case WAITING_TIME:
                // 默認方式：從等待隊列中按等待時間選擇
                selectedMembers = selectByWaitingTime(teamId, neededPlayers, allPlayersOnCourt);
                break;
            case RANDOM:
                // 隨機分配
                selectedMembers = selectRandomly(teamId, neededPlayers, allPlayersOnCourt);
                break;
            case ROUND_ROBIN:
                // 輪換分配
                selectedMembers = selectRoundRobin(teamId, neededPlayers, allPlayersOnCourt);
                break;
            default:
                // 默認使用等待時間
                selectedMembers = selectByWaitingTime(teamId, neededPlayers, allPlayersOnCourt);
        }
        
        return selectedMembers;
    }
    
    /**
     * 按等待時間選擇成員（默認方式）
     * 從queues表中獲取WAITING狀態的隊列，按等待時間排序（等待時間最長的優先）
     * 如果等待隊列為空，則從團隊成員中選擇（排除已在場上的）
     */
    private List<UserTeamDTO> selectByWaitingTime(Long teamId, int neededPlayers, Set<Long> allPlayersOnCourt) {
        // 從queues表獲取WAITING狀態的隊列
        // QueueServiceImpl已經按QUEUE_NUMBER.asc(), CREATED_AT.asc()排序（等待時間最長的優先）
        List<QueueDTO> waitingQueues = queueService.getQueuesByTeamIdAndStatus(teamId, Queue.QueueStatus.WAITING);
        
        // 獲取團隊成員列表（用於匹配User）
        List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
        if (teamMembers == null) {
            teamMembers = new ArrayList<>();
        }
        
        // 如果等待隊列為空，從團隊成員中選擇（排除已在場上的）
        if (waitingQueues == null || waitingQueues.isEmpty()) {
            log.info("團隊 {} 沒有等待隊列，從團隊成員中選擇（排除已在場上的）", teamId);
            List<UserTeamDTO> availableMembers = teamMembers.stream()
                    .filter(member -> !allPlayersOnCourt.contains(member.getUserId()))
                    .limit(neededPlayers)
                    .collect(Collectors.toList());
            log.info("從團隊成員中選擇了 {} 位成員（需要 {} 位）", availableMembers.size(), neededPlayers);
            return availableMembers;
        }
        
        // 確保按createdAt升序排序（等待時間最長的優先，即最早創建的）
        // 如果queueNumber相同，則按createdAt排序
        waitingQueues.sort((q1, q2) -> {
            // 首先按queueNumber排序（升序，號碼小的優先）
            if (q1.getQueueNumber() != null && q2.getQueueNumber() != null) {
                int queueNumberCompare = q1.getQueueNumber().compareTo(q2.getQueueNumber());
                if (queueNumberCompare != 0) {
                    return queueNumberCompare;
                }
            }
            // 如果queueNumber相同或為null，按createdAt排序（升序，最早創建的優先）
            if (q1.getCreatedAt() == null && q2.getCreatedAt() == null) return 0;
            if (q1.getCreatedAt() == null) return 1;
            if (q2.getCreatedAt() == null) return -1;
            return q1.getCreatedAt().compareTo(q2.getCreatedAt());
        });
        
        // 從等待隊列中選擇成員（選擇等待時間最長的neededPlayers位）
        List<UserTeamDTO> selected = new ArrayList<>();
        Set<Long> selectedUserIds = new HashSet<>(); // 用於去重
        
        for (QueueDTO queue : waitingQueues) {
            if (selected.size() >= neededPlayers) {
                break;
            }
            
            // 通過playerId獲取Player實體
            Player player = playerRepository.selectOneById(queue.getPlayerId());
            if (player == null) {
                log.warn("無法找到Player: playerId={}", queue.getPlayerId());
                continue;
            }
            
            // 通過Player的name匹配User
            String playerName = player.getName();
            UserTeamDTO matchedMember = teamMembers.stream()
                    .filter(m -> !allPlayersOnCourt.contains(m.getUserId()))
                    .filter(m -> !selectedUserIds.contains(m.getUserId())) // 避免重複選擇
                    .filter(m -> playerName.equals(m.getUserRealName()) || playerName.equals(m.getUserName()))
                    .findFirst()
                    .orElse(null);
            
            if (matchedMember != null) {
                selected.add(matchedMember);
                selectedUserIds.add(matchedMember.getUserId());
                log.debug("選擇等待者: userId={}, playerName={}, queueNumber={}, createdAt={}", 
                        matchedMember.getUserId(), playerName, queue.getQueueNumber(), queue.getCreatedAt());
            } else {
                log.warn("無法匹配等待隊列中的Player到User: playerId={}, playerName={}", 
                        queue.getPlayerId(), playerName);
            }
        }
        
        log.info("按等待時間選擇了 {} 位成員（需要 {} 位，等待隊列總數: {}）", 
                selected.size(), neededPlayers, waitingQueues.size());
        return selected;
    }
    
    /**
     * 隨機選擇成員
     */
    private List<UserTeamDTO> selectRandomly(Long teamId, int neededPlayers, Set<Long> allPlayersOnCourt) {
        List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
        if (teamMembers == null) {
            teamMembers = new ArrayList<>();
        }
        
        List<UserTeamDTO> availableMembers = teamMembers.stream()
                .filter(member -> !allPlayersOnCourt.contains(member.getUserId()))
                .collect(Collectors.toList());
        
        // 隨機打亂順序
        java.util.Collections.shuffle(availableMembers);
        
        int count = Math.min(neededPlayers, availableMembers.size());
        return availableMembers.subList(0, count);
    }
    
    /**
     * 輪換選擇成員（簡單實現：按順序選擇）
     */
    private List<UserTeamDTO> selectRoundRobin(Long teamId, int neededPlayers, Set<Long> allPlayersOnCourt) {
        // 簡單實現：按順序選擇（可以後續改進為真正的輪換邏輯）
        List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
        if (teamMembers == null) {
            teamMembers = new ArrayList<>();
        }
        
        List<UserTeamDTO> availableMembers = teamMembers.stream()
                .filter(member -> !allPlayersOnCourt.contains(member.getUserId()))
                .collect(Collectors.toList());
        
        int count = Math.min(neededPlayers, availableMembers.size());
        return availableMembers.subList(0, count);
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

