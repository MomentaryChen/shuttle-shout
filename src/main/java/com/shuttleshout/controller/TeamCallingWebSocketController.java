package com.shuttleshout.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 團隊叫號系統WebSocket控制器
 * 處理實時消息推送和接收
 * 
 * @author ShuttleShout Team
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TeamCallingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 處理隊列更新消息
     * 當有玩家加入或離開隊列時調用
     */
    @MessageMapping("/queue/update")
    @SendTo("/topic/queue")
    public Map<String, Object> handleQueueUpdate(@Payload Map<String, Object> message) {
        log.info("收到隊列更新消息: {}", message);
        Map<String, Object> response = new HashMap<>();
        response.put("type", "QUEUE_UPDATE");
        response.put("queue", message.get("queue"));
        response.put("teamId", message.get("teamId"));
        return response;
    }

    /**
     * 處理場地更新消息
     * 當場地狀態改變時調用
     */
    @MessageMapping("/court/update")
    @SendTo("/topic/court")
    public Map<String, Object> handleCourtUpdate(@Payload Map<String, Object> message) {
        log.info("收到場地更新消息: {}", message);
        Map<String, Object> response = new HashMap<>();
        response.put("type", "COURT_UPDATE");
        response.put("court", message.get("court"));
        response.put("teamId", message.get("teamId"));
        return response;
    }

    /**
     * 處理玩家分配到場地消息
     */
    @MessageMapping("/player/assigned")
    @SendTo("/topic/player")
    public Map<String, Object> handlePlayerAssigned(@Payload Map<String, Object> message) {
        log.info("收到玩家分配消息: {}", message);
        Map<String, Object> response = new HashMap<>();
        response.put("type", "PLAYER_ASSIGNED");
        response.put("playerId", message.get("playerId"));
        response.put("courtId", message.get("courtId"));
        response.put("position", message.get("position"));
        response.put("teamId", message.get("teamId"));
        return response;
    }

    /**
     * 處理玩家從場地移除消息
     */
    @MessageMapping("/player/removed")
    @SendTo("/topic/player")
    public Map<String, Object> handlePlayerRemoved(@Payload Map<String, Object> message) {
        log.info("收到玩家移除消息: {}", message);
        Map<String, Object> response = new HashMap<>();
        response.put("type", "PLAYER_REMOVED");
        response.put("playerId", message.get("playerId"));
        response.put("courtId", message.get("courtId"));
        response.put("teamId", message.get("teamId"));
        return response;
    }

    /**
     * 向特定團隊發送消息
     * 用於向特定團隊的所有連接客戶端推送消息
     */
    public void sendToTeam(Integer teamId, String destination, Object payload) {
        String topic = "/topic/team/" + teamId + destination;
        messagingTemplate.convertAndSend(topic, payload);
        log.info("向團隊 {} 發送消息到 {}: {}", teamId, topic, payload);
    }
}
