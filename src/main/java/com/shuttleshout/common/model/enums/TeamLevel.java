package com.shuttleshout.common.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 球隊等級枚舉
 * 
 * @author ShuttleShout Team
 */
public enum TeamLevel {
    /**
     * 新手
     */
    BEGINNER("新手"),
    
    /**
     * 初階
     */
    ELEMENTARY("初階"),
    
    /**
     * 中等
     */
    INTERMEDIATE("中等"),
    
    /**
     * 強
     */
    STRONG("強"),
    
    /**
     * 超強
     */
    VERY_STRONG("超強"),
    
    /**
     * 世界強
     */
    WORLD_CLASS("世界強");

    private final String displayName;

    TeamLevel(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 獲取顯示名稱
     * JSON 序列化時使用此方法返回的值
     * 
     * @return 顯示名稱
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根據顯示名稱獲取枚舉值
     * 
     * @param displayName 顯示名稱
     * @return 對應的枚舉值，如果未找到則返回 null
     */
    public static TeamLevel fromDisplayName(String displayName) {
        for (TeamLevel level : TeamLevel.values()) {
            if (level.displayName.equals(displayName)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 根據枚舉名稱獲取枚舉值（不區分大小寫）
     * 
     * @param name 枚舉名稱
     * @return 對應的枚舉值，如果未找到則返回 null
     */
    public static TeamLevel fromName(String name) {
        try {
            return TeamLevel.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * JSON 反序列化時使用此方法
     * 支援顯示名稱和枚舉名稱兩種格式
     * 
     * @param value JSON 中的值
     * @return 對應的枚舉值
     */
    @JsonCreator
    public static TeamLevel fromJson(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // 首先嘗試通過顯示名稱匹配
        TeamLevel level = fromDisplayName(value);
        if (level != null) {
            return level;
        }
        
        // 如果顯示名稱匹配失敗，嘗試通過枚舉名稱匹配
        level = fromName(value);
        if (level != null) {
            return level;
        }
        
        // 如果都匹配失敗，拋出異常
        throw new IllegalArgumentException("無效的球隊等級: " + value);
    }
}

