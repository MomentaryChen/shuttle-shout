package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.QueueTableDef.QUEUE;
import static com.shuttleshout.common.model.po.table.PlayerTableDef.PLAYER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.QueueDTO;
import com.shuttleshout.common.model.po.Court;
import com.shuttleshout.common.model.po.Player;
import com.shuttleshout.common.model.po.Queue;
import com.shuttleshout.repository.CourtRepository;
import com.shuttleshout.repository.PlayerRepository;
import com.shuttleshout.repository.QueueRepository;
import com.shuttleshout.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 隊列服務實現類
 * 
 * @author ShuttleShout Team
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QueueServiceImpl extends ServiceImpl<QueueRepository, Queue> implements QueueService {

    private final PlayerRepository playerRepository;
    private final CourtRepository courtRepository;

    @Override
    public List<QueueDTO> getAllQueues() {
        List<Queue> queues = getMapper().selectAll();
        return queues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public QueueDTO getQueueById(Long id) {
        Queue queue = getMapper().selectOneById(id);
        if (queue == null) {
            throw new ApiException(ErrorCode.QUEUE_NOT_FOUND, "隊列不存在，ID: " + id);
        }
        return convertToDto(queue);
    }

    @Override
    public List<QueueDTO> getQueuesByPlayerId(Long playerId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(QUEUE.PLAYER_ID.eq(playerId))
                .orderBy(QUEUE.CREATED_AT.asc());
        List<Queue> queues = getMapper().selectListByQuery(queryWrapper);
        return queues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QueueDTO> getQueuesByCourtId(Long courtId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(QUEUE.COURT_ID.eq(courtId))
                .orderBy(QUEUE.CREATED_AT.asc());
        List<Queue> queues = getMapper().selectListByQuery(queryWrapper);
        return queues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QueueDTO> getQueuesByTeamIdAndStatus(Long teamId, Queue.QueueStatus status) {
        // 先查詢該團隊的所有球員ID
        QueryWrapper playerQuery = QueryWrapper.create()
                .where(PLAYER.TEAM_ID.eq(teamId));
        List<Player> players = playerRepository.selectListByQuery(playerQuery);
        
        if (players == null || players.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> playerIds = players.stream()
                .map(Player::getId)
                .collect(Collectors.toList());
        
        // 查詢這些球員的指定狀態的隊列
        QueryWrapper queueQuery = QueryWrapper.create()
                .where(QUEUE.PLAYER_ID.in(playerIds))
                .and(QUEUE.STATUS.eq(status.toString()))
                .orderBy(QUEUE.QUEUE_NUMBER.asc(), QUEUE.CREATED_AT.asc());
        List<Queue> queues = getMapper().selectListByQuery(queueQuery);
        
        return queues.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Queue createQueue(Long playerId, Long courtId, Queue.QueueStatus status) {
        // 驗證球員是否存在
        Player player = playerRepository.selectOneById(playerId);
        if (player == null) {
            throw new ApiException(ErrorCode.PLAYER_NOT_FOUND, "球員不存在，ID: " + playerId);
        }

        // 如果提供了場地ID，驗證場地是否存在
        if (courtId != null) {
            Court court = courtRepository.selectOneById(courtId);
            if (court == null) {
                throw new ApiException(ErrorCode.COURT_NOT_FOUND, "場地不存在，ID: " + courtId);
            }
        }

        // 計算排隊號碼（獲取該場地或全局的最大排隊號碼）
        Integer queueNumber = calculateNextQueueNumber(courtId);

        LocalDateTime now = LocalDateTime.now();
        Queue queue = new Queue();
        queue.setPlayerId(playerId);
        queue.setCourtId(courtId);
        queue.setStatus(status);
        queue.setQueueNumber(queueNumber);
        if (status == Queue.QueueStatus.CALLED) {
            queue.setCalledAt(now);
        }
        if (status == Queue.QueueStatus.SERVED) {
            queue.setServedAt(now);
        }
        queue.setCreatedAt(now);
        queue.setUpdatedAt(now);

        getMapper().insert(queue);
        log.info("成功創建隊列記錄: queueId={}, playerId={}, courtId={}, status={}, queueNumber={}", 
                queue.getId(), playerId, courtId, status, queueNumber);
        
        return queue;
    }

    @Override
    public QueueDTO updateQueueStatus(Long queueId, Queue.QueueStatus status) {
        Queue queue = getMapper().selectOneById(queueId);
        if (queue == null) {
            throw new ApiException(ErrorCode.QUEUE_NOT_FOUND, "隊列不存在，ID: " + queueId);
        }

        queue.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        
        if (status == Queue.QueueStatus.CALLED && queue.getCalledAt() == null) {
            queue.setCalledAt(now);
        }
        if (status == Queue.QueueStatus.SERVED && queue.getServedAt() == null) {
            queue.setServedAt(now);
        }
        
        queue.setUpdatedAt(now);
        getMapper().update(queue);
        
        return convertToDto(queue);
    }

    @Override
    public void deleteQueue(Long id) {
        Queue queue = getMapper().selectOneById(id);
        if (queue == null) {
            throw new ApiException(ErrorCode.QUEUE_NOT_FOUND, "隊列不存在，ID: " + id);
        }
        getMapper().deleteById(id);
    }

    @Override
    public int deleteWaitingQueuesByTeamId(Long teamId) {
        // 先查詢該團隊的所有球員ID
        QueryWrapper playerQuery = QueryWrapper.create()
                .where(PLAYER.TEAM_ID.eq(teamId));
        List<Player> players = playerRepository.selectListByQuery(playerQuery);
        
        if (players == null || players.isEmpty()) {
            log.info("團隊 {} 沒有球員，無需刪除隊列", teamId);
            return 0;
        }
        
        List<Long> playerIds = players.stream()
                .map(Player::getId)
                .collect(Collectors.toList());
        
        // 查詢這些球員的WAITING狀態的隊列
        QueryWrapper queueQuery = QueryWrapper.create()
                .where(QUEUE.PLAYER_ID.in(playerIds))
                .and(QUEUE.STATUS.eq(Queue.QueueStatus.WAITING.toString()));
        List<Queue> waitingQueues = getMapper().selectListByQuery(queueQuery);
        
        if (waitingQueues == null || waitingQueues.isEmpty()) {
            log.info("團隊 {} 沒有WAITING狀態的隊列", teamId);
            return 0;
        }
        
        // 刪除所有WAITING狀態的隊列
        int deletedCount = 0;
        for (Queue queue : waitingQueues) {
            try {
                getMapper().deleteById(queue.getId());
                deletedCount++;
            } catch (Exception e) {
                log.error("刪除隊列失敗: queueId={}", queue.getId(), e);
            }
        }
        
        log.info("已刪除團隊 {} 的 {} 個WAITING狀態的隊列", teamId, deletedCount);
        return deletedCount;
    }

    /**
     * 計算下一個排隊號碼
     */
    private Integer calculateNextQueueNumber(Long courtId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (courtId != null) {
            queryWrapper.where(QUEUE.COURT_ID.eq(courtId));
        }
        queryWrapper.orderBy(QUEUE.QUEUE_NUMBER.desc());
        queryWrapper.limit(1);
        
        Queue lastQueue = getMapper().selectOneByQuery(queryWrapper);
        if (lastQueue != null && lastQueue.getQueueNumber() != null) {
            return lastQueue.getQueueNumber() + 1;
        }
        return 1;
    }

    /**
     * 轉換為DTO
     */
    private QueueDTO convertToDto(Queue queue) {
        QueueDTO dto = new QueueDTO();
        dto.setId(queue.getId());
        dto.setPlayerId(queue.getPlayerId());
        dto.setCourtId(queue.getCourtId());
        dto.setStatus(queue.getStatus());
        dto.setQueueNumber(queue.getQueueNumber());
        dto.setCalledAt(queue.getCalledAt());
        dto.setServedAt(queue.getServedAt());
        dto.setCreatedAt(queue.getCreatedAt());
        dto.setUpdatedAt(queue.getUpdatedAt());
        
        // 填充關聯信息
        if (queue.getPlayer() != null) {
            dto.setPlayerName(queue.getPlayer().getName());
        }
        if (queue.getCourt() != null) {
            dto.setCourtName(queue.getCourt().getName());
        }
        
        return dto;
    }
}

