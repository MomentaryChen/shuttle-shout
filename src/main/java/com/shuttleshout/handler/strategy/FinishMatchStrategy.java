package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.handler.TeamCallingWebSocketHandler;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;
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
            
            // 獲取該場地正在進行的比賽
            Match match = matchService.getOngoingMatchByCourtId(courtId);
            if (match == null) {
                log.warn("場地 {} 沒有正在進行的比賽", courtId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("message", "該場地沒有正在進行的比賽");
                handler.sendMessage(session, handler.createMessage("ERROR", errorData));
                return;
            }
            
            // 結束比賽（這會將比賽狀態設為 FINISHED，球員信息保留在 matches 表中）
            matchService.finishMatch(match.getId());
            log.info("比賽已結束: matchId={}, courtId={}", match.getId(), courtId);
            
            // 注意：不再需要清空 team_courts 表的球員，因為球員信息現在只存儲在 matches 表中
            
            // 構建響應消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "MATCH_FINISHED");
            response.put("courtId", courtId);
            response.put("teamId", teamIdObj);
            response.put("message", "比賽已結束，場地已清空");
            
            // 廣播給所有客戶端
            handler.broadcastMessage(response);
            
            // 更新等待隊列（球員已放回等待隊列）
            if (teamIdObj != null) {
                Long teamId = handler.convertToLong(teamIdObj);
                if (teamId != null) {
                    handler.sendWaitingQueueUpdate(null, teamId, null); // null表示廣播，第三個參數為null表示自動計算
                }
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

