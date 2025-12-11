package com.shuttleshout.common.model.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.shuttleshout.common.model.enums.TeamLevel;

/**
 * 創建球隊數據傳輸對象
 * 
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreateDTO {

    @NotBlank(message = "球隊名稱不能為空")
    private String name;

    private String description; // 球隊描述

    @NotNull(message = "用戶ID不能為空")
    private Long userId; // 球隊所屬用戶ID（必填）

    private String color; // 球隊顏色標識，如 "bg-blue-500"

    private TeamLevel level; // 球隊等級

    @Min(value = 1, message = "最大人數必須大於0")
    private Integer maxPlayers = 20; // 球隊最大人數

    @Min(value = 1, message = "場地數量必須大於0")
    private Integer courtCount = 2; // 球隊分配的場地數量
}

