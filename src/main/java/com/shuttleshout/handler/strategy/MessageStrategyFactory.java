package com.shuttleshout.handler.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息策略工廠
 * 負責管理和查找所有的WebSocket消息處理策略
 * 自動注入所有實現 WebSocketMessageStrategy 的組件
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Component
public class MessageStrategyFactory {
    
    private final Map<String, WebSocketMessageStrategy> strategies = new HashMap<>();
    
    @Autowired(required = false)
    private List<WebSocketMessageStrategy> allStrategies;
    
    /**
     * 初始化所有策略
     * 自動註冊所有實現 WebSocketMessageStrategy 的 Spring 組件
     */
    @PostConstruct
    public void initialize() {
        if (allStrategies != null) {
            for (WebSocketMessageStrategy strategy : allStrategies) {
                registerStrategy(strategy);
            }
        }
        log.info("消息策略工廠初始化完成，註冊了 {} 個策略", strategies.size());
    }
    
    /**
     * 註冊策略
     * 
     * @param strategy 策略實例
     */
    private void registerStrategy(WebSocketMessageStrategy strategy) {
        String messageType = strategy.getMessageType();
        if (messageType != null && !messageType.isEmpty()) {
            strategies.put(messageType, strategy);
            log.debug("註冊策略: {}", messageType);
        } else {
            log.warn("策略 {} 的消息類型為空，跳過註冊", strategy.getClass().getSimpleName());
        }
    }
    
    /**
     * 根據消息類型獲取對應的策略
     * 
     * @param messageType 消息類型
     * @return 對應的策略，如果不存在則返回null
     */
    public WebSocketMessageStrategy getStrategy(String messageType) {
        return strategies.get(messageType);
    }
    
    /**
     * 檢查是否支持某個消息類型
     * 
     * @param messageType 消息類型
     * @return 是否支持
     */
    public boolean supportsMessageType(String messageType) {
        return strategies.containsKey(messageType);
    }
}

