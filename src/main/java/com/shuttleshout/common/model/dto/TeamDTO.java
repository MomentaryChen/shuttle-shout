package com.shuttleshout.common.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.shuttleshout.common.model.enums.TeamLevel;

/**
 * 球隊數據傳輸對象
 * 
 * @author ShuttleShout Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {

    private Long id;

    @NotBlank(message = "球隊名稱不能為空")
    private String name;

    private String description; // 球隊描述

    private String color;

    private TeamLevel level; // 球隊等級

    @Min(value = 1, message = "最大人數必須大於0")
    private Integer maxPlayers;

    @Min(value = 1, message = "場地數量必須大於0")
    private Integer courtCount;

    private Boolean isActive;

    private Long userId; // 球隊所屬用戶ID

    private Integer currentPlayerCount; // 當前球員數量\

    private List<Long> playerIds; // 當前球員ID列表

    private Integer currentCourtCount; // 當前場地數量

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
