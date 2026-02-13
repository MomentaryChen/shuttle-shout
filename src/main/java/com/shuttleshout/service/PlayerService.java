package com.shuttleshout.service;

import java.util.List;

import com.shuttleshout.common.model.dto.PlayerDTO;
import com.shuttleshout.common.model.po.Player;

/**
 * 球員服務接口
 * 
 * @author ShuttleShout Team
 */
public interface PlayerService {

    /**
     * 獲取所有球員
     */
    List<PlayerDTO> getAllPlayers();

    /**
     * 根據ID獲取球員
     */
    PlayerDTO getPlayerById(Long id);

    /**
     * 根據團隊ID獲取球員列表
     */
    List<PlayerDTO> getPlayersByTeamId(Long teamId);

    /**
     * 創建球員（從用戶信息創建）
     * 
     * @param userId 用戶ID
     * @param teamId 團隊ID
     * @return 創建的球員
     */
    Player createPlayerFromUser(Long userId, Long teamId);

    /**
     * 創建球員
     */
    PlayerDTO createPlayer(Player player);

    /**
     * 更新球員
     */
    PlayerDTO updatePlayer(Long id, Player player);

    /**
     * 刪除球員
     */
    void deletePlayer(Long id);
}

