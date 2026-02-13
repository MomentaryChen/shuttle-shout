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
import com.shuttleshout.common.model.po.TeamPO;
import com.shuttleshout.repository.CourtRepository;
import com.shuttleshout.repository.TeamRepository;
import com.shuttleshout.service.CourtService;

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

    @Override
    public List<Court> getAllCourts() {
        return getMapper().selectAll();
    }

    @Override
    public List<Court> getActiveCourts() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(COURT.IS_ACTIVE.eq(true))
                .orderBy(COURT.TEAM_ID.asc(), COURT.ID.asc());
        return getMapper().selectListByQuery(queryWrapper);
    }

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
    @Transactional
    public Court updateCourt(Court court) {
        if (court.getUpdatedAt() == null) {
            court.setUpdatedAt(LocalDateTime.now());
        }
        getMapper().update(court);
        return court;
    }
    
    /**
     * 清空場地的球員信息和比賽時間
     * 使用 SQL 直接更新，確保 null 值被正確更新到數據庫
     * 
     * @param courtId 場地ID
     */
    @Transactional
    public void clearCourtPlayers(Long courtId) {
        LocalDateTime now = LocalDateTime.now();
        getMapper().clearCourtPlayers(courtId, now, now);
        log.info("已使用 SQL 清空場地 {} 的球員信息和比賽時間", courtId);
    }

    @Override
    public List<Long> getPlayersOnCourt(Long courtId) {
        // 從 court 表中獲取該場地的球員
        Court court = getCourtById(courtId);
        List<Long> players = new ArrayList<>();
        
        if (court != null) {
            players.add(court.getPlayer1Id());
            players.add(court.getPlayer2Id());
            players.add(court.getPlayer3Id());
            players.add(court.getPlayer4Id());
        }
        
        return players;
    }
}

