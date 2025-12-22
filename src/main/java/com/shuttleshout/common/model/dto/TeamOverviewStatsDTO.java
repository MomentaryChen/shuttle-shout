package com.shuttleshout.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 團隊總覽統計數據傳輸對象
 * 
 * @author ShuttleShout Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamOverviewStatsDTO {

    /**
     * 總人數（所有活躍團隊的成員總數）
     */
    private Integer totalPlayers;

    /**
     * 使用場地（所有活躍團隊使用的場地總數）
     */
    private Integer totalCourts;

    /**
     * 活躍團隊數量
     */
    private Integer activeTeams;
}

