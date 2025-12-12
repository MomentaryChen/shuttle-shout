package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.MatchTableDef.MATCH;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.po.Match;
import com.shuttleshout.repository.MatchRepository;
import com.shuttleshout.service.MatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 比賽服務實現類
 * 
 * @author ShuttleShout Team
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl extends ServiceImpl<MatchRepository, Match> implements MatchService {

    @Override
    @Transactional
    public Match createMatch(Long teamId, Long courtId, Long player1Id, Long player2Id, Long player3Id, Long player4Id) {
        LocalDateTime now = LocalDateTime.now();
        
        Match match = Match.builder()
                .teamId(teamId)
                .courtId(courtId)
                .player1Id(player1Id)
                .player2Id(player2Id)
                .player3Id(player3Id)
                .player4Id(player4Id)
                .status(Match.MatchStatus.ONGOING)
                .startedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        getMapper().insert(match);
        log.info("創建比賽記錄: matchId={}, teamId={}, courtId={}, players=[{}, {}, {}, {}]", 
                match.getId(), teamId, courtId, player1Id, player2Id, player3Id, player4Id);
        
        return match;
    }

    @Override
    @Transactional
    public Match createPendingMatch(Long teamId, Long courtId, Long player1Id, Long player2Id, Long player3Id, Long player4Id) {
        LocalDateTime now = LocalDateTime.now();
        
        Match match = Match.builder()
                .teamId(teamId)
                .courtId(courtId)
                .player1Id(player1Id)
                .player2Id(player2Id)
                .player3Id(player3Id)
                .player4Id(player4Id)
                .status(Match.MatchStatus.PENDING_CONFIRMATION)
                .startedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        getMapper().insert(match);
        log.info("創建等待確認的比賽記錄: matchId={}, teamId={}, courtId={}, players=[{}, {}, {}, {}]", 
                match.getId(), teamId, courtId, player1Id, player2Id, player3Id, player4Id);
        
        return match;
    }

    @Override
    public Match getMatchById(Long id) {
        Match match = getMapper().selectOneById(id);
        if (match == null) {
            throw new ApiException(ErrorCode.COURT_NOT_FOUND, "比賽不存在，ID: " + id);
        }
        return match;
    }

    @Override
    public Match getOngoingMatchByCourtId(Long courtId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(MATCH.COURT_ID.eq(courtId))
                .and(MATCH.STATUS.in(
                        Match.MatchStatus.PENDING_CONFIRMATION.name(),
                        Match.MatchStatus.ONGOING.name()))
                .orderBy(MATCH.STARTED_AT.desc())
                .limit(1);
        return getMapper().selectOneByQuery(queryWrapper);
    }

    @Override
    public List<Match> getMatchesByTeamId(Long teamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(MATCH.TEAM_ID.eq(teamId))
                .orderBy(MATCH.STARTED_AT.desc());
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    public List<Match> getOngoingMatchesByTeamId(Long teamId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(MATCH.TEAM_ID.eq(teamId))
                .and(MATCH.STATUS.in(
                        Match.MatchStatus.PENDING_CONFIRMATION.name(),
                        Match.MatchStatus.ONGOING.name()))
                .orderBy(MATCH.STARTED_AT.desc());
        return getMapper().selectListByQuery(queryWrapper);
    }

    @Override
    @Transactional
    public Match finishMatch(Long matchId) {
        Match match = getMatchById(matchId);
        match.setStatus(Match.MatchStatus.FINISHED);
        match.setEndedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());
        getMapper().update(match);
        log.info("比賽已結束: matchId={}", matchId);
        return match;
    }

    @Override
    @Transactional
    public Match cancelMatch(Long matchId) {
        Match match = getMatchById(matchId);
        match.setStatus(Match.MatchStatus.CANCELLED);
        match.setEndedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());
        getMapper().update(match);
        log.info("比賽已取消: matchId={}", matchId);
        return match;
    }

    @Override
    @Transactional
    public Match confirmMatch(Long matchId) {
        Match match = getMatchById(matchId);
        if (match.getStatus() != Match.MatchStatus.PENDING_CONFIRMATION) {
            throw new ApiException(ErrorCode.ILLEGAL_STATE, 
                    "只有等待確認狀態的比賽才能被確認，當前狀態: " + match.getStatus());
        }
        match.setStatus(Match.MatchStatus.ONGOING);
        match.setStartedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());
        getMapper().update(match);
        log.info("比賽已確認: matchId={}", matchId);
        return match;
    }
}

