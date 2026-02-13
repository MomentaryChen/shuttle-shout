package com.shuttleshout.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import com.shuttleshout.handler.strategy.MessageStrategyFactory;
import com.shuttleshout.handler.strategy.WebSocketMessageStrategy;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;
import com.shuttleshout.service.QueueService;
import com.shuttleshout.service.UserTeamService;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.repository.PlayerRepository;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.common.model.dto.QueueDTO;
import com.shuttleshout.common.model.po.Queue;
import com.shuttleshout.common.model.po.Player;
import static com.shuttleshout.common.model.po.table.UserPOTableDef.USER_PO;
import com.mybatisflex.core.query.QueryWrapper;

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
    private final QueueService queueService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    
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
        
        // 如果有teamId，檢查是否有進行中的比賽，發送狀態檢查消息給前端
        // 不自動恢復狀態，等待用戶選擇"開始新局"或"恢復狀態"
        if (teamIdStr != null && !teamIdStr.isEmpty()) {
            try {
                Long teamId = convertToLong(teamIdStr);
                if (teamId != null) {
                    checkAndSendGameState(session, teamId);
                }
            } catch (Exception e) {
                log.error("檢查團隊狀態失敗: teamId={}", teamIdStr, e);
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
     * 檢查並發送遊戲狀態（是否有進行中的比賽）
     * 如果有進行中的比賽，發送狀態檢查消息，等待用戶選擇
     * 
     * @param session WebSocket會話
     * @param teamId 團隊ID
     */
    private void checkAndSendGameState(WebSocketSession session, Long teamId) {
        try {
            // 獲取團隊的所有場地
            List<Court> courts = courtService.getCourtsByTeamId(teamId);
            
            if (courts == null || courts.isEmpty()) {
                log.debug("團隊 {} 沒有場地", teamId);
                // 沒有場地，發送無狀態消息
                Map<String, Object> stateData = new HashMap<>();
                stateData.put("type", "GAME_STATE_CHECK");
                stateData.put("teamId", teamId);
                stateData.put("hasOngoingMatches", false);
                stateData.put("message", "沒有進行中的比賽，可以開始新的一局");
                sendMessage(session, stateData);
                return;
            }
            
            // 檢查是否有進行中的比賽
            int ongoingCount = 0;
            for (Court court : courts) {
                boolean hasPlayers = court.getPlayer1Id() != null || 
                                     court.getPlayer2Id() != null || 
                                     court.getPlayer3Id() != null || 
                                     court.getPlayer4Id() != null;
                if (hasPlayers) {
                    ongoingCount++;
                }
            }
            
            // 發送狀態檢查消息
            Map<String, Object> stateData = new HashMap<>();
            stateData.put("type", "GAME_STATE_CHECK");
            stateData.put("teamId", teamId);
            stateData.put("hasOngoingMatches", ongoingCount > 0);
            stateData.put("ongoingCourtsCount", ongoingCount);
            if (ongoingCount > 0) {
                stateData.put("message", String.format("檢測到 %d 個場地有進行中的比賽，請選擇：開始新的一局 或 恢復上次狀態", ongoingCount));
            } else {
                stateData.put("message", "沒有進行中的比賽，可以開始新的一局");
            }
            sendMessage(session, stateData);
            log.info("已發送遊戲狀態檢查消息: teamId={}, hasOngoingMatches={}, ongoingCourtsCount={}", 
                    teamId, ongoingCount > 0, ongoingCount);
            
        } catch (Exception e) {
            log.error("檢查遊戲狀態失敗: teamId={}", teamId, e);
        }
    }
    
    /**
     * 加載並發送進行中比賽的人員信息
     * 每個場地只取最新的一筆進行中比賽數據
     * 
     * @param session WebSocket會話
     * @param teamId 團隊ID
     */
    public void loadAndSendOngoingMatches(WebSocketSession session, Long teamId) {
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
            
            // 構建每個場地的分配信息（從 court 表讀取球員信息）
            List<Map<String, Object>> courtsData = new ArrayList<>();
            int ongoingCount = 0;
            
            for (Court court : courts) {
                // 檢查場地上是否有球員（從 court 表讀取）
                boolean hasPlayers = court.getPlayer1Id() != null || 
                                     court.getPlayer2Id() != null || 
                                     court.getPlayer3Id() != null || 
                                     court.getPlayer4Id() != null;
                
                if (hasPlayers) {
                    ongoingCount++;
                    Map<String, Object> courtData = new HashMap<>();
                    courtData.put("courtId", court.getId());
                    
                    // 添加比賽開始時間
                    if (court.getMatchStartedAt() != null) {
                        courtData.put("matchStartedAt", court.getMatchStartedAt().toString());
                    }
                    
                    // 構建該場地的球員分配列表（從 court 表讀取）
                    List<Map<String, Object>> assignments = new ArrayList<>();
                    
                    if (court.getPlayer1Id() != null) {
                        assignments.add(buildPlayerAssignment(court.getPlayer1Id(), 1, memberMap));
                    }
                    if (court.getPlayer2Id() != null) {
                        assignments.add(buildPlayerAssignment(court.getPlayer2Id(), 2, memberMap));
                    }
                    if (court.getPlayer3Id() != null) {
                        assignments.add(buildPlayerAssignment(court.getPlayer3Id(), 3, memberMap));
                    }
                    if (court.getPlayer4Id() != null) {
                        assignments.add(buildPlayerAssignment(court.getPlayer4Id(), 4, memberMap));
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
     * 從queues表中查詢WAITING狀態的記錄
     * 需要被策略類訪問，因此設為 public
     * 
     * @param session WebSocket會話
     * @param teamId 團隊ID
     */
    public void loadAndSendWaitingQueue(WebSocketSession session, Long teamId) {
        try {
            // 從queues表查詢該團隊的WAITING狀態的隊列
            List<QueueDTO> waitingQueues = queueService.getQueuesByTeamIdAndStatus(teamId, Queue.QueueStatus.WAITING);
            
            if (waitingQueues == null || waitingQueues.isEmpty()) {
                log.debug("團隊 {} 沒有等待隊列", teamId);
                sendWaitingQueueUpdate(session, teamId, new ArrayList<>());
                return;
            }
            
            // 獲取團隊的所有成員（用於匹配User）
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            if (teamMembers == null) {
                teamMembers = new ArrayList<>();
            }
            
            // 將Queue轉換為UserTeamDTO，並保持排序順序（等待時間最長的優先）
            // 使用LinkedHashMap保持插入順序，確保按照Queue的排序順序
            Map<Long, UserTeamDTO> memberMap = new LinkedHashMap<>();
            Map<Long, QueueDTO> queueMap = new HashMap<>(); // 用於存儲queue信息，方便排序
            
            for (QueueDTO queue : waitingQueues) {
                // 通過playerId獲取Player信息
                Player player = playerRepository.selectOneById(queue.getPlayerId());
                if (player == null) {
                    log.warn("無法找到Player: playerId={}", queue.getPlayerId());
                    continue;
                }
                
                // 通過Player的name和phoneNumber匹配User
                String playerName = player.getName();
                String playerPhone = player.getPhoneNumber();
                
                UserTeamDTO matchedMember = null;
                
                // 首先嘗試通過name匹配（realName或username）
                if (playerName != null) {
                    matchedMember = teamMembers.stream()
                            .filter(m -> playerName.equals(m.getUserRealName()) || playerName.equals(m.getUserName()))
                            .findFirst()
                            .orElse(null);
                }
                
                // 如果name匹配失敗，嘗試通過phoneNumber匹配
                if (matchedMember == null && playerPhone != null) {
                    QueryWrapper userQuery = QueryWrapper.create()
                            .where(USER_PO.PHONE_NUMBER.eq(playerPhone));
                    UserPO user = userRepository.selectOneByQuery(userQuery);
                    if (user != null) {
                        matchedMember = teamMembers.stream()
                                .filter(m -> m.getUserId().equals(user.getId()))
                                .findFirst()
                                .orElse(null);
                    }
                }
                
                if (matchedMember != null) {
                    // 使用LinkedHashMap保持插入順序，按照Queue的順序添加
                    // 如果該成員已經存在（可能有多個Queue記錄），只保留第一個（等待時間最長的）
                    if (!memberMap.containsKey(matchedMember.getUserId())) {
                        memberMap.put(matchedMember.getUserId(), matchedMember);
                        queueMap.put(matchedMember.getUserId(), queue);
                    } else {
                        // 如果已存在，比較queueNumber和createdAt，保留等待時間更長的
                        QueueDTO existingQueue = queueMap.get(matchedMember.getUserId());
                        boolean shouldReplace = false;
                        
                        if (queue.getQueueNumber() != null && existingQueue.getQueueNumber() != null) {
                            if (queue.getQueueNumber() < existingQueue.getQueueNumber()) {
                                shouldReplace = true;
                            } else if (queue.getQueueNumber().equals(existingQueue.getQueueNumber())) {
                                // queueNumber相同，比較createdAt
                                if (queue.getCreatedAt() != null && existingQueue.getCreatedAt() != null) {
                                    if (queue.getCreatedAt().isBefore(existingQueue.getCreatedAt())) {
                                        shouldReplace = true;
                                    }
                                }
                            }
                        } else if (queue.getCreatedAt() != null && existingQueue.getCreatedAt() != null) {
                            if (queue.getCreatedAt().isBefore(existingQueue.getCreatedAt())) {
                                shouldReplace = true;
                            }
                        }
                        
                        if (shouldReplace) {
                            memberMap.put(matchedMember.getUserId(), matchedMember);
                            queueMap.put(matchedMember.getUserId(), queue);
                        }
                    }
                } else {
                    log.warn("無法匹配Player到User: playerId={}, playerName={}, playerPhone={}", 
                            queue.getPlayerId(), playerName, playerPhone);
                }
            }
            
            // 將Map轉換為List，保持順序（等待時間最長的在前）
            // 需要按照queueMap中的queue信息重新排序
            List<UserTeamDTO> waitingQueue = new ArrayList<>(memberMap.values());
            
            // 按照queueNumber和createdAt排序（等待時間最長的優先）
            waitingQueue.sort((a, b) -> {
                QueueDTO queueA = queueMap.get(a.getUserId());
                QueueDTO queueB = queueMap.get(b.getUserId());
                
                if (queueA == null && queueB == null) return 0;
                if (queueA == null) return 1;
                if (queueB == null) return -1;
                
                // 首先按queueNumber排序（升序，號碼小的優先）
                if (queueA.getQueueNumber() != null && queueB.getQueueNumber() != null) {
                    int queueNumberCompare = queueA.getQueueNumber().compareTo(queueB.getQueueNumber());
                    if (queueNumberCompare != 0) {
                        return queueNumberCompare;
                    }
                }
                
                // 如果queueNumber相同或為null，按createdAt排序（升序，最早創建的優先）
                if (queueA.getCreatedAt() == null && queueB.getCreatedAt() == null) return 0;
                if (queueA.getCreatedAt() == null) return 1;
                if (queueB.getCreatedAt() == null) return -1;
                return queueA.getCreatedAt().compareTo(queueB.getCreatedAt());
            });
            
            log.info("團隊 {} 的等待隊列: {} 人（從queues表查詢）", teamId, waitingQueue.size());
            
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
                    
                    // 收集所有在場地上的球員ID（從 court 表中獲取）
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
            
            // 如果teamId不為null，從queues表獲取Queue信息並按等待時間從短到長排序
            Map<Long, QueueDTO> userIdToQueueMap = new HashMap<>(); // 移到外部作用域
            if (teamId != null && !queueToSend.isEmpty()) {
                try {
                    // 獲取該團隊的所有WAITING狀態的Queue
                    List<QueueDTO> waitingQueues = queueService.getQueuesByTeamIdAndStatus(teamId, Queue.QueueStatus.WAITING);
                    
                    if (waitingQueues != null && !waitingQueues.isEmpty()) {
                        
                        // 通過Player匹配UserTeamDTO
                        for (QueueDTO queueDto : waitingQueues) {
                            Player player = playerRepository.selectOneById(queueDto.getPlayerId());
                            if (player == null) {
                                continue;
                            }
                            
                            // 通過Player的name或phoneNumber匹配UserTeamDTO
                            String playerName = player.getName();
                            String playerPhone = player.getPhoneNumber();
                            
                            for (UserTeamDTO member : queueToSend) {
                                boolean matched = false;
                                
                                // 通過name匹配
                                if (playerName != null && 
                                    (playerName.equals(member.getUserRealName()) || playerName.equals(member.getUserName()))) {
                                    matched = true;
                                }
                                
                                // 如果name匹配失敗，嘗試通過phoneNumber匹配
                                if (!matched && playerPhone != null) {
                                    QueryWrapper userQuery = QueryWrapper.create()
                                            .where(USER_PO.PHONE_NUMBER.eq(playerPhone));
                                    UserPO user = userRepository.selectOneByQuery(userQuery);
                                    if (user != null && user.getId().equals(member.getUserId())) {
                                        matched = true;
                                    }
                                }
                                
                                if (matched) {
                                    // 如果該userId已經有Queue，保留等待時間更長的（queueNumber更小或createdAt更早）
                                    QueueDTO existingQueue = userIdToQueueMap.get(member.getUserId());
                                    if (existingQueue == null) {
                                        userIdToQueueMap.put(member.getUserId(), queueDto);
                                    } else {
                                        boolean shouldReplace = false;
                                        
                                        // 比較queueNumber（小的優先，即等待時間更長）
                                        if (queueDto.getQueueNumber() != null && existingQueue.getQueueNumber() != null) {
                                            if (queueDto.getQueueNumber() < existingQueue.getQueueNumber()) {
                                                shouldReplace = true;
                                            } else if (queueDto.getQueueNumber().equals(existingQueue.getQueueNumber())) {
                                                // queueNumber相同，比較createdAt（早的優先，即等待時間更長）
                                                if (queueDto.getCreatedAt() != null && existingQueue.getCreatedAt() != null) {
                                                    if (queueDto.getCreatedAt().isBefore(existingQueue.getCreatedAt())) {
                                                        shouldReplace = true;
                                                    }
                                                }
                                            }
                                        } else if (queueDto.getCreatedAt() != null && existingQueue.getCreatedAt() != null) {
                                            if (queueDto.getCreatedAt().isBefore(existingQueue.getCreatedAt())) {
                                                shouldReplace = true;
                                            }
                                        }
                                        
                                        if (shouldReplace) {
                                            userIdToQueueMap.put(member.getUserId(), queueDto);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        
                        // 按照等待時間從長到短排序（等待時間最長的優先，queueNumber小的在前，或createdAt早的在前）
                        final Map<Long, QueueDTO> finalQueueMap = userIdToQueueMap;
                        queueToSend.sort((a, b) -> {
                            QueueDTO queueA = finalQueueMap.get(a.getUserId());
                            QueueDTO queueB = finalQueueMap.get(b.getUserId());
                            
                            // 如果都沒有Queue信息，保持原順序
                            if (queueA == null && queueB == null) return 0;
                            if (queueA == null) return 1; // 沒有Queue的排在後面
                            if (queueB == null) return -1; // 沒有Queue的排在後面
                            
                            // 首先按queueNumber排序（升序，號碼小的優先，即等待時間長的在前）
                            if (queueA.getQueueNumber() != null && queueB.getQueueNumber() != null) {
                                int queueNumberCompare = queueA.getQueueNumber().compareTo(queueB.getQueueNumber());
                                if (queueNumberCompare != 0) {
                                    return queueNumberCompare;
                                }
                            }
                            
                            // 如果queueNumber相同或為null，按createdAt排序（升序，早創建的優先，即等待時間長的在前）
                            if (queueA.getCreatedAt() == null && queueB.getCreatedAt() == null) return 0;
                            if (queueA.getCreatedAt() == null) return 1;
                            if (queueB.getCreatedAt() == null) return -1;
                            return queueA.getCreatedAt().compareTo(queueB.getCreatedAt());
                        });
                    }
                } catch (Exception e) {
                    log.warn("獲取Queue信息失敗，使用原順序: {}", e.getMessage());
                    // 如果獲取Queue信息失敗，繼續使用原順序
                }
            }
            
            // 構建隊列數據（轉換為Map列表），包含時間信息
            final Map<Long, QueueDTO> finalQueueMapForData = userIdToQueueMap; // 使用外部作用域的userIdToQueueMap
            List<Map<String, Object>> queueData = queueToSend.stream()
                    .map(member -> {
                        Map<String, Object> memberData = new HashMap<>();
                        memberData.put("userId", member.getUserId());
                        memberData.put("userName", member.getUserName());
                        memberData.put("userRealName", member.getUserRealName());
                        memberData.put("userEmail", member.getUserEmail());
                        
                        // 添加隊列時間信息（如果存在）
                        QueueDTO queueDto = finalQueueMapForData.get(member.getUserId());
                        if (queueDto != null && queueDto.getCreatedAt() != null) {
                            memberData.put("queueCreatedAt", queueDto.getCreatedAt().toString());
                        }
                        
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
