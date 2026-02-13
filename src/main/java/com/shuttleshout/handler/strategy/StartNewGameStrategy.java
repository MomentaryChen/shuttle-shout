package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.common.model.dto.UserTeamDTO;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Player;
import com.shuttleshout.common.model.po.Queue;
import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.PlayerService;
import com.shuttleshout.service.QueueService;
import com.shuttleshout.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;

/**
 * 開始新局策略
 * 清空所有場地的狀態，開始新的一局
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class StartNewGameStrategy implements WebSocketMessageStrategy {
    
    @Lazy
    @Autowired
    private TeamCallingWebSocketHandler handler;
    
    @Autowired
    private CourtService courtService;
    
    @Autowired
    private UserTeamService userTeamService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private QueueService queueService;
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> data) {
        try {
            Object teamIdObj = data.get("teamId");
            
            log.info("收到開始新局請求: teamId={}", teamIdObj);
            
            // 驗證參數
            if (teamIdObj == null) {
                log.warn("開始新局請求缺少必要參數: teamId");
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "缺少必要參數: teamId");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            Long teamId = handler.convertToLong(teamIdObj);
            
            if (teamId == null) {
                log.warn("參數類型無效: teamId={}", teamIdObj);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "參數類型無效");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 獲取團隊的所有場地
            List<Court> courts = courtService.getCourtsByTeamId(teamId);
            
            if (courts == null || courts.isEmpty()) {
                log.warn("團隊 {} 沒有場地", teamId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "團隊沒有場地");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 清空所有場地的狀態
            int clearedCount = 0;
            for (Court court : courts) {
                try {
                    courtService.clearCourtPlayers(court.getId());
                    clearedCount++;
                    log.info("已清空場地 {} 的狀態", court.getId());
                } catch (Exception e) {
                    log.error("清空場地 {} 的狀態失敗", court.getId(), e);
                }
            }
            
            // 刪除現有的WAITING狀態的隊列
            int deletedQueueCount = queueService.deleteWaitingQueuesByTeamId(teamId);
            log.info("已刪除團隊 {} 的 {} 個WAITING狀態的隊列", teamId, deletedQueueCount);
            
            // 獲取團隊的所有成員
            List<UserTeamDTO> teamMembers = userTeamService.getTeamMembers(teamId);
            if (teamMembers == null || teamMembers.isEmpty()) {
                log.warn("團隊 {} 沒有成員，無法創建隊列", teamId);
            } else {
                // 為每個成員創建Player記錄和WAITING狀態的Queue記錄
                int createdQueueCount = 0;
                for (UserTeamDTO member : teamMembers) {
                    try {
                        // 創建Player記錄（從用戶信息創建，如果已存在則返回現有記錄）
                        Player player = playerService.createPlayerFromUser(member.getUserId(), teamId);
                        
                        // 創建WAITING狀態的Queue記錄
                        Queue queue = queueService.createQueue(player.getId(), null, Queue.QueueStatus.WAITING);
                        createdQueueCount++;
                        log.debug("為成員 {} 創建隊列記錄: queueId={}, playerId={}", 
                                member.getUserId(), queue.getId(), player.getId());
                    } catch (Exception e) {
                        log.error("為成員 {} 創建Player或Queue記錄失敗", member.getUserId(), e);
                        // 繼續處理其他成員，不影響整體流程
                    }
                }
                log.info("為團隊 {} 的 {} 位成員創建了 {} 個隊列記錄", teamId, teamMembers.size(), createdQueueCount);
            }
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "START_NEW_GAME_SUCCESS");
            response.put("teamId", teamId);
            response.put("clearedCourtsCount", clearedCount);
            response.put("deletedQueueCount", deletedQueueCount);
            response.put("message", String.format("已清空 %d 個場地的狀態，已為所有成員創建等待隊列", clearedCount));
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列（發送新創建的隊列）
            handler.sendWaitingQueueUpdate(null, teamId, null);
            
            log.info("開始新局請求處理完成: teamId={}, clearedCourtsCount={}, deletedQueueCount={}", 
                    teamId, clearedCount, deletedQueueCount);
        } catch (Exception e) {
            log.error("處理開始新局請求失敗", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("message", "開始新局失敗: " + e.getMessage());
            handler.sendMessage(session, handler.createMessage("ERROR", errorData));
        }
    }
    
    @Override
    public String getMessageType() {
        return "START_NEW_GAME";
    }
}

