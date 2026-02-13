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
     * 獲取所有場地
     */
    List<Court> getAllCourts();

    /**
     * 獲取所有活躍場地
     */
    List<Court> getActiveCourts();

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
     * 更新場地
     * 
     * @param court 場地實體
     * @return 更新後的場地
     */
    Court updateCourt(Court court);

    /**
     * 清空場地的球員信息和比賽時間
     * 使用 UpdateWrapper 確保 null 值被正確更新到數據庫
     * 
     * @param courtId 場地ID
     */
    void clearCourtPlayers(Long courtId);

    /**
     * 獲取場地上正在進行的比賽的球員ID列表
     * 從 court 表中讀取球員信息
     * 
     * @param courtId 場地ID
     * @return 球員ID列表（按位置1-4順序，可能包含null）
     */
    List<Long> getPlayersOnCourt(Long courtId);
}

