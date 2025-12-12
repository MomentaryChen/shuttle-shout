package com.shuttleshout.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuttleshout.common.model.dto.UserTeamDTO;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.handler.strategy.MessageStrategyFactory;
import com.shuttleshout.handler.strategy.WebSocketMessageStrategy;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;
import com.shuttleshout.service.UserTeamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * 團隊叫號系統WebSocket處理器
 * 處理原生WebSocket連接和消息
 * 
 * @author ShuttleShout Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TeamCallingWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageStrategyFactory strategyFactory;
    private final MatchService matchService;
    private final UserTeamService userTeamService;
    private final CourtService courtService;
    
    /**
     * 初始化完成
     */
    @PostConstruct
    public void init() {
        log.info("TeamCallingWebSocketHandler 初始化完成");
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        // 從查詢參數中獲取teamId
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        String teamIdStr = extractTeamId(query);
        
        log.info("WebSocket連接建立: sessionId={}, teamId={}", sessionId, teamIdStr);
        
        // 發送連接成功消息
        Map<String, Object> data = new HashMap<>();
        data.put("teamId", teamIdStr != null ? teamIdStr : "");
        sendMessage(session, createMessage("CONNECTED", data));
        
        // 如果有teamId，加載並發送進行中比賽的人員信息和等待隊列
        if (teamIdStr != null && !teamIdStr.isEmpty()) {
            try {
                Long teamId = convertToLong(teamIdStr);
                if (teamId != null) {
                    loadAndSendOngoingMatches(session, teamId);
                    loadAndSendWaitingQueue(session, teamId);
                }
            } catch (Exception e) {
                log.error("加載團隊數據失敗: teamId={}", teamIdStr, e);
                // 不影響連接，只記錄錯誤
            }
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        log.info("收到WebSocket消息: sessionId={}, payload={}", sessionId, payload);
        
        try {
            // 解析收到的消息
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");
            
            // 使用策略模式處理消息
            WebSocketMessageStrategy strategy = strategyFactory.getStrategy(type);
            if (strategy != null) {
                log.debug("使用策略處理消息: type={}, strategy={}", type, strategy.getClass().getSimpleName());
                strategy.handle(session, data);
            } else {
                log.warn("未知的消息類型: {}", type);
            }
        } catch (Exception e) {
            log.error("處理WebSocket消息失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "消息處理失敗");
            sendMessage(session, createMessage("ERROR", errorData));
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("WebSocket連接關閉: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("WebSocket傳輸錯誤: sessionId={}", session.getId(), exception);
        sessions.remove(session.getId());
    }

    /**
     * 發送消息給特定會話
     */
    public void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("發送WebSocket消息失敗", e);
        }
    }

    /**
     * 廣播消息給所有連接的客戶端
     */
    public void broadcastMessage(Map<String, Object> message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化消息失敗", e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                log.error("廣播消息失敗: sessionId={}", session.getId(), e);
            }
        }
    }

    /**
     * 創建標準消息格式
     * 需要被策略類訪問，因此設為 public
     */
    public Map<String, Object> createMessage(String type, Map<String, Object> data) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", type);
        message.putAll(data);
        return message;
    }


    /**
     * 從對象中提取球員ID
     * 需要被策略類訪問，因此設為 public
     */
    public Long extractPlayerId(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> playerMap = (Map<String, Object>) obj;
            return convertToLong(playerMap.get("id"));
        }
        return convertToLong(obj);
    }

    /**
     * 將對象轉換為 Long 類型
     * 需要被策略類訪問，因此設為 public
     */
    public Long convertToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                log.warn("無法將字符串轉換為 Long: {}", obj);
                return null;
            }
        }
        return null;
    }

    /**
     * 將對象轉換為 Integer 類型
     * 需要被策略類訪問，因此設為 public
     */
    public Integer convertToInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                log.warn("無法將字符串轉換為 Integer: {}", obj);
                return null;
            }
        }
        return null;
    }

    /**
     * 從查詢字符串中提取teamId
     */
    private String extractTeamId(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] params = query.split("&");
        for (String param : params) {
            if (param != null && param.contains("=")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "teamId".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    /**
     * 加載並發送進行中比賽的人員信息
     * 每個場地只取最新的一筆進行中比賽數據
     * 
     * @param session WebSocket會話
     * @param teamId 團隊ID
     */
    private void loadAndSendOngoingMatches(WebSocketSession session, Long teamId) {
        try {
            // 獲取團隊的所有場地
            List<Court> courts = courtService.getCourtsByTeamId(teamId);
            
            if (courts == null || courts.isEmpty()) {
                log.debug("團隊 {} 沒有場地", teamId);
                return;
            }
            
            // 獲取團隊成員列表，用於構建用戶信息映射
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            Map<Long, UserTeamDTO> memberMap = teamMembers.stream()
                    .collect(Collectors.toMap(UserTeamDTO::getUserId, member -> member));
            
            // 構建每個場地的分配信息（每個場地只取最新的一筆進行中比賽）
            List<Map<String, Object>> courtsData = new ArrayList<>();
            int ongoingCount = 0;
            
            for (Court court : courts) {
                // 獲取該場地最新的一筆進行中比賽
                Match match = matchService.getOngoingMatchByCourtId(court.getId());
                
                if (match != null) {
                    ongoingCount++;
                    Map<String, Object> courtData = new HashMap<>();
                    courtData.put("courtId", court.getId());
                    
                    // 構建該場地的球員分配列表
                    List<Map<String, Object>> assignments = new ArrayList<>();
                    
                    if (match.getPlayer1Id() != null) {
                        assignments.add(buildPlayerAssignment(match.getPlayer1Id(), 1, memberMap));
                    }
                    if (match.getPlayer2Id() != null) {
                        assignments.add(buildPlayerAssignment(match.getPlayer2Id(), 2, memberMap));
                    }
                    if (match.getPlayer3Id() != null) {
                        assignments.add(buildPlayerAssignment(match.getPlayer3Id(), 3, memberMap));
                    }
                    if (match.getPlayer4Id() != null) {
                        assignments.add(buildPlayerAssignment(match.getPlayer4Id(), 4, memberMap));
                    }
                    
                    courtData.put("assignments", assignments);
                    courtsData.add(courtData);
                }
            }
            
            if (ongoingCount == 0) {
                log.debug("團隊 {} 沒有進行中的比賽", teamId);
                return;
            }
            
            log.info("團隊 {} 有 {} 個場地有進行中的比賽，開始加載人員信息", teamId, ongoingCount);
            
            // 發送恢復數據消息
            Map<String, Object> restoreData = new HashMap<>();
            restoreData.put("type", "RESTORE_ONGOING_MATCHES");
            restoreData.put("teamId", teamId);
            restoreData.put("courts", courtsData);
            restoreData.put("message", String.format("已恢復 %d 個場地的進行中比賽", courtsData.size()));
            
            sendMessage(session, restoreData);
            log.info("成功發送進行中比賽恢復數據: teamId={}, courtsCount={}", teamId, courtsData.size());
            
        } catch (Exception e) {
            log.error("加載進行中比賽失敗: teamId={}", teamId, e);
        }
    }

    /**
     * 構建球員分配信息
     * 
     * @param userId 用戶ID
     * @param position 位置 (1-4)
     * @param memberMap 成員映射表
     * @return 分配信息Map
     */
    private Map<String, Object> buildPlayerAssignment(Long userId, int position, Map<Long, UserTeamDTO> memberMap) {
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

    /**
     * 加載並發送等待隊列
     * 等待隊列 = 團隊成員 - 所有場地上的球員
     * 
     * @param session WebSocket會話
     * @param teamId 團隊ID
     */
    private void loadAndSendWaitingQueue(WebSocketSession session, Long teamId) {
        try {
            // 獲取團隊的所有成員
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            if (teamMembers == null || teamMembers.isEmpty()) {
                log.debug("團隊 {} 沒有成員", teamId);
                sendWaitingQueueUpdate(session, teamId, new ArrayList<>());
                return;
            }
            
            // 獲取團隊的所有場地
            List<Court> courts = courtService.getCourtsByTeamId(teamId);
            
            // 收集所有在場地上的球員ID（從 matches 表中獲取）
            Set<Long> playersOnCourt = new HashSet<>();
            if (courts != null) {
                for (Court court : courts) {
                    List<Long> players = courtService.getPlayersOnCourt(court.getId());
                    for (Long playerId : players) {
                        if (playerId != null) {
                            playersOnCourt.add(playerId);
                        }
                    }
                }
            }
            
            // 過濾出不在場地上的成員（即等待隊列）
            List<UserTeamDTO> waitingQueue = teamMembers.stream()
                    .filter(member -> !playersOnCourt.contains(member.getUserId()))
                    .collect(Collectors.toList());
            
            log.info("團隊 {} 的等待隊列: {} 人（總成員: {} 人，場上: {} 人）", 
                    teamId, waitingQueue.size(), teamMembers.size(), playersOnCourt.size());
            
            // 發送等待隊列更新
            sendWaitingQueueUpdate(session, teamId, waitingQueue);
            
        } catch (Exception e) {
            log.error("加載等待隊列失敗: teamId={}", teamId, e);
        }
    }

    /**
     * 發送等待隊列更新消息
     * 
     * @param session WebSocket會話（如果為null則廣播給所有客戶端）
     * @param teamId 團隊ID
     * @param queue 等待隊列（如果為null則自動計算：團隊成員 - 所有場地上的球員）
     */
    public void sendWaitingQueueUpdate(WebSocketSession session, Long teamId, List<UserTeamDTO> queue) {
        try {
            List<UserTeamDTO> queueToSend = queue;
            
            // 如果queue為null，自動計算等待隊列
            if (queueToSend == null && teamId != null) {
                // 獲取團隊的所有成員
                List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
                if (teamMembers == null || teamMembers.isEmpty()) {
                    queueToSend = new ArrayList<>();
                } else {
                    // 獲取團隊的所有場地
                    List<Court> courts = courtService.getCourtsByTeamId(teamId);
                    
                    // 收集所有在場地上的球員ID（從 matches 表中獲取）
                    Set<Long> playersOnCourt = new HashSet<>();
                    if (courts != null) {
                        for (Court court : courts) {
                            List<Long> players = courtService.getPlayersOnCourt(court.getId());
                            for (Long playerId : players) {
                                if (playerId != null) {
                                    playersOnCourt.add(playerId);
                                }
                            }
                        }
                    }
                    
                    // 過濾出不在場地上的成員（即等待隊列）
                    queueToSend = teamMembers.stream()
                            .filter(member -> !playersOnCourt.contains(member.getUserId()))
                            .collect(Collectors.toList());
                }
            }
            
            if (queueToSend == null) {
                log.warn("無法計算等待隊列: teamId={}", teamId);
                return;
            }
            
            // 構建隊列數據（轉換為Map列表）
            List<Map<String, Object>> queueData = queueToSend.stream()
                    .map(member -> {
                        Map<String, Object> memberData = new HashMap<>();
                        memberData.put("userId", member.getUserId());
                        memberData.put("userName", member.getUserName());
                        memberData.put("userRealName", member.getUserRealName());
                        memberData.put("userEmail", member.getUserEmail());
                        return memberData;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> queueUpdate = new HashMap<>();
            queueUpdate.put("type", "QUEUE_UPDATE");
            queueUpdate.put("teamId", teamId);
            queueUpdate.put("queue", queueData);
            
            if (session != null) {
                // 發送給特定會話
                sendMessage(session, queueUpdate);
            } else {
                // 廣播給所有客戶端
                broadcastMessage(queueUpdate);
            }
            
            log.debug("已發送等待隊列更新: teamId={}, queueSize={}", teamId, queueData.size());
        } catch (Exception e) {
            log.error("發送等待隊列更新失敗", e);
        }
    }
}
