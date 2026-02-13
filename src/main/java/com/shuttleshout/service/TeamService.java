package com.shuttleshout.service;

import java.util.List;

import javax.validation.Valid;

import com.shuttleshout.common.model.dto.TeamCreateDTO;
import com.shuttleshout.common.model.dto.TeamDTO;
import com.shuttleshout.common.model.dto.TeamOverviewStatsDTO;
import com.shuttleshout.common.model.dto.TeamUpdateDTO;

/**
 * 球隊服務接口
 * 
 * @author ShuttleShout Team
 */
public interface TeamService {

    /**
     * 獲取所有球隊
     */
    List<TeamDTO> getAllTeams();

    /**
     * 根據用戶ID獲取該用戶創建的球隊
     */
    List<TeamDTO> getTeamsByUserId(Long userId);

    /**
     * 根據ID獲取球隊
     */
    TeamDTO getTeamById(Long id);

    /**
     * 根據用戶ID獲取球隊
     */
    List<Long> getTeamsPlayerIdsByTeamId(Long teamId);

    /**
     * 根據名稱獲取球隊
     */
    TeamDTO getTeamByName(String name);

    /**
     * 創建球隊
     */
    TeamDTO createTeam(@Valid TeamCreateDTO teamCreateDto);

    /**
     * 更新球隊
     */
    TeamDTO updateTeam(Long id, @Valid TeamUpdateDTO teamUpdateDto);

    /**
     * 刪除球隊
     */
    void deleteTeam(Long id);

    /**
     * 獲取團隊總覽統計數據（總人數和使用場地）
     */
    TeamOverviewStatsDTO getTeamOverviewStats();
}

