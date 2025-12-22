package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.CourtTableDef.COURT;
import static com.shuttleshout.common.model.po.table.TeamPOTableDef.TEAM_PO;
import static com.shuttleshout.common.model.po.table.UserTeamPOTableDef.USER_TEAM_PO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.constants.ApplicationConstants;
import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.TeamCreateDTO;
import com.shuttleshout.common.model.dto.TeamDTO;
import com.shuttleshout.common.model.dto.TeamOverviewStatsDTO;
import com.shuttleshout.common.model.dto.TeamUpdateDTO;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.TeamPO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.common.model.po.UserTeamPO;
import com.shuttleshout.repository.CourtRepository;
import com.shuttleshout.repository.TeamRepository;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.repository.UserTeamRepository;
import com.shuttleshout.service.TeamService;

import lombok.RequiredArgsConstructor;

/**
 * 球隊服務實現類
 * 
 * @author ShuttleShout Team
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamRepository, TeamPO> implements TeamService {

    private final UserRepository userRepository;

    private final UserTeamRepository userTeamRepository;

    private final CourtRepository courtRepository;

    /**
     * 獲取所有球隊
     */
    @Override
    public List<TeamDTO> getAllTeams() {
        List<TeamPO> teams = getMapper().selectAll();

        // 轉換為DTO（包含playerIds和currentPlayerCount）
        return teams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據用戶ID獲取該用戶創建的球隊
     */
    @Override
    public List<TeamDTO> getTeamsByUserId(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(TEAM_PO.USER_ID.eq(userId));
        List<TeamPO> teams = getMapper().selectListByQuery(queryWrapper);

        // 轉換為DTO（包含playerIds和currentPlayerCount）
        return teams.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據ID獲取球隊
     */
    @Override
    public TeamDTO getTeamById(Long id) {
        TeamPO team = getMapper().selectOneById(id);
        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "球隊不存在，ID: " + id);
        }
        // convertToDto已經包含playerIds和currentPlayerCount
        return convertToDto(team);
    }

    /**
     * 根據名稱獲取球隊
     */
    @Override
    public TeamDTO getTeamByName(String name) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(TEAM_PO.NAME.eq(name));
        TeamPO team = getMapper().selectOneByQuery(queryWrapper);
        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "球隊不存在，名稱: " + name);
        }
        return convertToDto(team);
    }

    /**
     * 創建球隊
     */
    @Override
    @Transactional
    public TeamDTO createTeam(@Valid TeamCreateDTO teamCreateDto) {
        // 檢查用戶是否存在
        UserPO user = userRepository.selectOneById(teamCreateDto.getUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + teamCreateDto.getUserId());
        }

        // 檢查球隊名稱是否已存在
        validateTeamNameNotExists(teamCreateDto.getName(), null);

        // 創建球隊
        LocalDateTime now = LocalDateTime.now();
        TeamPO team = TeamPO.builder()
                .name(teamCreateDto.getName())
                .description(teamCreateDto.getDescription())
                .userId(user.getId())
                .color(teamCreateDto.getColor())
                .level(teamCreateDto.getLevel())
                .maxPlayers(teamCreateDto.getMaxPlayers() != null ? teamCreateDto.getMaxPlayers()
                        : ApplicationConstants.MAX_PLAYERS)
                .courtCount(teamCreateDto.getCourtCount() != null ? teamCreateDto.getCourtCount()
                        : ApplicationConstants.MAX_COURTS)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        getMapper().insert(team);

        // 預設加入自己
        UserTeamPO userTeam = UserTeamPO.builder()
                .teamId(team.getId())
                .userId(user.getId())
                .isOwner(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userTeamRepository.insert(userTeam);

        return convertToDto(team);
    }

    /**
     * 更新球隊
     */
    @Override
    @Transactional
    public TeamDTO updateTeam(Long id, @Valid TeamUpdateDTO teamUpdateDto) {
        TeamPO team = getMapper().selectOneById(id);
        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "球隊不存在，ID: " + id);
        }

        // 檢查用戶是否存在（如果提供了 userId）
        if (teamUpdateDto.getUserId() != null) {
            UserPO user = userRepository.selectOneById(teamUpdateDto.getUserId());
            if (user == null) {
                throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶不存在，ID: " + teamUpdateDto.getUserId());
            }
        }

        // 檢查球隊名稱是否已被其他球隊使用
        if (teamUpdateDto.getName() != null && !teamUpdateDto.getName().equals(team.getName())) {
            validateTeamNameNotExists(teamUpdateDto.getName(), id);
        }

        if (teamUpdateDto.getName() != null) {
            team.setName(teamUpdateDto.getName());
        }
        if (teamUpdateDto.getDescription() != null) {
            team.setDescription(teamUpdateDto.getDescription());
        }
        if (teamUpdateDto.getUserId() != null) {
            team.setUserId(teamUpdateDto.getUserId());
        }
        if (teamUpdateDto.getColor() != null) {
            team.setColor(teamUpdateDto.getColor());
        }
        if (teamUpdateDto.getLevel() != null) {
            team.setLevel(teamUpdateDto.getLevel());
        }
        if (teamUpdateDto.getMaxPlayers() != null) {
            team.setMaxPlayers(teamUpdateDto.getMaxPlayers());
        }
        if (teamUpdateDto.getCourtCount() != null) {
            team.setCourtCount(teamUpdateDto.getCourtCount());
        }
        if (teamUpdateDto.getIsActive() != null) {
            team.setIsActive(teamUpdateDto.getIsActive());
        }
        team.setUpdatedAt(LocalDateTime.now());

        getMapper().update(team);

        return convertToDto(team);
    }

    /**
     * 刪除球隊
     */
    @Override
    @Transactional
    public void deleteTeam(Long id) {
        TeamPO team = getMapper().selectOneById(id);
        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "球隊不存在，ID: " + id);
        }

        // 注意：由於外鍵約束設置為 ON DELETE SET NULL，刪除球隊時，關聯的球員和場地的 team_id 會被設置為 NULL
        // 如果需要強制刪除，可以在這裡添加額外的邏輯

        getMapper().deleteById(id);
    }

    /**
     * 驗證球隊名稱是否已存在
     * 
     * @param teamName      球隊名稱
     * @param excludeTeamId 排除的球隊ID（用於更新時排除當前球隊），如果為null則檢查所有球隊
     * @throws RuntimeException 如果球隊名稱已存在
     */
    private void validateTeamNameNotExists(String teamName, Long excludeTeamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(TEAM_PO.NAME.eq(teamName));
        if (excludeTeamId != null) {
            queryWrapper.and(TEAM_PO.ID.ne(excludeTeamId));
        }
        TeamPO existingTeam = getMapper().selectOneByQuery(queryWrapper);
        if (existingTeam != null) {
            throw new ApiException(ErrorCode.TEAM_NAME_ALREADY_EXISTS, "球隊名稱已存在: " + teamName);
        }
    }

    /**
     * 根據球隊ID獲取球隊用戶
     */
    @Override
    public List<Long> getTeamsPlayerIdsByTeamId(Long teamId) {
        List<UserTeamPO> userTeams = userTeamRepository.selectListByQuery(QueryWrapper.create()
                .where(USER_TEAM_PO.TEAM_ID.eq(teamId)));
        if (userTeams == null) {
            return new ArrayList<>();
        }
        return userTeams.stream().map(UserTeamPO::getUserId).collect(Collectors.toList());
    }

    /**
     * 獲取團隊總覽統計數據（總人數和使用場地）
     */
    @Override
    public TeamOverviewStatsDTO getTeamOverviewStats() {
        // 獲取所有活躍團隊的ID列表
        QueryWrapper activeTeamsQuery = QueryWrapper.create()
                .where(TEAM_PO.IS_ACTIVE.eq(true));
        List<TeamPO> activeTeams = getMapper().selectListByQuery(activeTeamsQuery);
        
        if (activeTeams == null || activeTeams.isEmpty()) {
            return TeamOverviewStatsDTO.builder()
                    .totalPlayers(0)
                    .totalCourts(0)
                    .activeTeams(0)
                    .build();
        }

        List<Long> activeTeamIds = activeTeams.stream()
                .map(TeamPO::getId)
                .collect(Collectors.toList());

        // 統計總人數：查詢所有活躍團隊的成員總數（去重，避免同一用戶加入多個團隊被重複計算）
        QueryWrapper userTeamQuery = QueryWrapper.create()
                .where(USER_TEAM_PO.TEAM_ID.in(activeTeamIds));
        List<UserTeamPO> userTeams = userTeamRepository.selectListByQuery(userTeamQuery);
        // 使用 Set 去重，統計不重複的用戶ID數量
        Set<Long> uniqueUserIds = new HashSet<>();
        if (userTeams != null) {
            for (UserTeamPO userTeam : userTeams) {
                uniqueUserIds.add(userTeam.getUserId());
            }
        }
        int totalPlayers = uniqueUserIds.size();

        // 統計使用場地：查詢所有活躍團隊的場地總數
        QueryWrapper courtQuery = QueryWrapper.create()
                .where(COURT.TEAM_ID.in(activeTeamIds));
        List<Court> courts = courtRepository.selectListByQuery(courtQuery);
        int totalCourts = courts != null ? courts.size() : 0;

        return TeamOverviewStatsDTO.builder()
                .totalPlayers(totalPlayers)
                .totalCourts(totalCourts)
                .activeTeams(activeTeams.size())
                .build();
    }

    /**
     * 轉換為DTO
     */
    private TeamDTO convertToDto(TeamPO team) {
        // 查詢當前球員數量和球員ID列表 - 通過user_teams表獲取球隊成員數量
        List<Long> playerIds = getTeamsPlayerIdsByTeamId(team.getId());
        int currentPlayerCount = playerIds.size();

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .color(team.getColor())
                .level(team.getLevel())
                .maxPlayers(team.getMaxPlayers())
                .courtCount(team.getCourtCount())
                .isActive(team.getIsActive())
                .userId(team.getUserId())
                .currentPlayerCount(currentPlayerCount)
                .playerIds(playerIds)
                .currentCourtCount(team.getCourts().size())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
