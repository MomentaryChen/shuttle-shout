package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.CourtTableDef.COURT;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.common.model.po.TeamPO;
import com.shuttleshout.repository.CourtRepository;
import com.shuttleshout.repository.TeamRepository;
import com.shuttleshout.service.CourtService;
import com.shuttleshout.service.MatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 場地服務實現類
 * 
 * @author ShuttleShout Team
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CourtServiceImpl extends ServiceImpl<CourtRepository, Court> implements CourtService {

    private final TeamRepository teamRepository;
    private final MatchService matchService;

    @Override
    public List<Court> getCourtsByTeamId(Long teamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(COURT.TEAM_ID.eq(teamId))
                .orderBy(COURT.ID.asc());
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional
    public List<Court> initializeCourtsForTeam(Long teamId) {
        // 獲取團隊信息
        TeamPO team = teamRepository.selectOneById(teamId);
        if (team == null) {
            throw new ApiException(ErrorCode.TEAM_NOT_FOUND, "團隊不存在，ID: " + teamId);
        }

        // 獲取現有場地
        List<Court> existingCourts = getCourtsByTeamId(teamId);
        int existingCount = existingCourts.size();
        int requiredCount = team.getCourtCount() != null ? team.getCourtCount() : 2;

        // 如果已有足夠的場地，直接返回
        if (existingCount >= requiredCount) {
            log.info("團隊 {} 已有 {} 個場地，無需創建", teamId, existingCount);
            return existingCourts;
        }

        // 創建缺失的場地
        List<Court> newCourts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = existingCount + 1; i <= requiredCount; i++) {
            Court court = Court.builder()
                    .name("場地 " + i)
                    .teamId(teamId)
                    .isActive(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            getMapper().insert(court);
            newCourts.add(court);
            log.info("為團隊 {} 創建場地: {}", teamId, court.getName());
        }

        // 返回所有場地（包括新創建的和已存在的）
        return getCourtsByTeamId(teamId);
    }

    @Override
    @Transactional
    public Court createCourt(Court court) {
        LocalDateTime now = LocalDateTime.now();
        if (court.getCreatedAt() == null) {
            court.setCreatedAt(now);
        }
        if (court.getUpdatedAt() == null) {
            court.setUpdatedAt(now);
        }
        getMapper().insert(court);
        return court;
    }

    @Override
    public Court getCourtById(Long id) {
        Court court = getMapper().selectOneById(id);
        if (court == null) {
            throw new ApiException(ErrorCode.COURT_NOT_FOUND, "場地不存在，ID: " + id);
        }
        return court;
    }

    @Override
    public List<Long> getPlayersOnCourt(Long courtId) {
        // 從 matches 表中獲取該場地正在進行的比賽的球員
        Match match = matchService.getOngoingMatchByCourtId(courtId);
        List<Long> players = new ArrayList<>();
        
        if (match != null) {
            players.add(match.getPlayer1Id());
            players.add(match.getPlayer2Id());
            players.add(match.getPlayer3Id());
            players.add(match.getPlayer4Id());
        }
        
        return players;
    }
}

