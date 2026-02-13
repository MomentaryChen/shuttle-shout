package com.shuttleshout.common.model.enums;

/**
 * 羽球等級（台灣羽球推廣協會分級制度）
 * 級數 1–18 對應比賽階級；後端儲存級數，前端顯示比賽階級與級數。
 *
 * @author ShuttleShout Team
 */
public enum BadmintonLevel {

    LEVEL_1(1, "新手階"),
    LEVEL_2(2, "新手階"),
    LEVEL_3(3, "新手階"),
    LEVEL_4(4, "初階"),
    LEVEL_5(5, "初階"),
    LEVEL_6(6, "初中階"),
    LEVEL_7(7, "初中階"),
    LEVEL_8(8, "中階"),
    LEVEL_9(9, "中階"),
    LEVEL_10(10, "中進階"),
    LEVEL_11(11, "中進階"),
    LEVEL_12(12, "中進階"),
    LEVEL_13(13, "高階"),
    LEVEL_14(14, "高階"),
    LEVEL_15(15, "高階"),
    LEVEL_16(16, "職業級"),
    LEVEL_17(17, "職業級"),
    LEVEL_18(18, "職業級");

    private final int level;
    private final String displayName; // 比賽階級

    BadmintonLevel(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 取得比賽階級顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 依級數（1–18）取得枚舉，非法值回傳 null
     */
    public static BadmintonLevel fromLevel(int level) {
        if (level < 1 || level > 18) {
            return null;
        }
        return BadmintonLevel.values()[level - 1];
    }

    /**
     * 級數是否在有效範圍 1–18
     */
    public static boolean isValid(Integer level) {
        return level != null && level >= 1 && level <= 18;
    }
}
