package com.shuttleshout.service;

import java.util.List;

import com.shuttleshout.common.model.po.Court;

/**
 * 場地服務接口
 * 
 * @author ShuttleShout Team
 */
public interface CourtService {

    /**
     * 根據團隊ID獲取所有場地
     * 
     * @param teamId 團隊ID
     * @return 場地列表
     */
    List<Court> getCourtsByTeamId(Long teamId);

    /**
     * 初始化團隊的所有場地
     * 如果場地不存在，則根據團隊的courtCount自動創建
     * 
     * @param teamId 團隊ID
     * @return 創建的場地列表
     */
    List<Court> initializeCourtsForTeam(Long teamId);

    /**
     * 創建場地
     * 
     * @param court 場地實體
     * @return 創建後的場地
     */
    Court createCourt(Court court);

    /**
     * 根據ID獲取場地
     * 
     * @param id 場地ID
     * @return 場地實體
     */
    Court getCourtById(Long id);

    /**
     * 獲取場地上正在進行的比賽的球員ID列表
     * 注意：球員信息現在只存儲在 matches 表中，不再存儲在 team_courts 表中
     * 
     * @param courtId 場地ID
     * @return 球員ID列表（按位置1-4順序，可能包含null）
     */
    List<Long> getPlayersOnCourt(Long courtId);
}

