package com.shuttleshout.service;

import java.util.List;

import com.shuttleshout.common.model.po.Match;

/**
 * 比賽服務接口
 * 
 * @author ShuttleShout Team
 */
public interface MatchService {

    /**
     * 創建比賽記錄
     * 
     * @param teamId 球隊ID
     * @param courtId 場地ID
     * @param player1Id 球員1的用戶ID
     * @param player2Id 球員2的用戶ID
     * @param player3Id 球員3的用戶ID
     * @param player4Id 球員4的用戶ID
     * @return 創建後的比賽記錄
     */
    Match createMatch(Long teamId, Long courtId, Long player1Id, Long player2Id, Long player3Id, Long player4Id);

    /**
     * 根據ID獲取比賽
     * 
     * @param id 比賽ID
     * @return 比賽實體
     */
    Match getMatchById(Long id);

    /**
     * 根據場地ID獲取正在進行的比賽
     * 
     * @param courtId 場地ID
     * @return 比賽實體，如果沒有則返回null
     */
    Match getOngoingMatchByCourtId(Long courtId);

    /**
     * 根據球隊ID獲取所有比賽
     * 
     * @param teamId 球隊ID
     * @return 比賽列表
     */
    List<Match> getMatchesByTeamId(Long teamId);

    /**
     * 根據球隊ID獲取所有進行中的比賽
     * 
     * @param teamId 球隊ID
     * @return 進行中的比賽列表
     */
    List<Match> getOngoingMatchesByTeamId(Long teamId);

    /**
     * 結束比賽
     * 
     * @param matchId 比賽ID
     * @return 更新後的比賽記錄
     */
    Match finishMatch(Long matchId);

    /**
     * 取消比賽
     * 
     * @param matchId 比賽ID
     * @return 更新後的比賽記錄
     */
    Match cancelMatch(Long matchId);
}

