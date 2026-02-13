package com.shuttleshout.service;

import java.util.List;

import com.shuttleshout.common.model.dto.QueueDTO;
import com.shuttleshout.common.model.po.Queue;

/**
 * 隊列服務接口
 * 
 * @author ShuttleShout Team
 */
public interface QueueService {

    /**
     * 獲取所有隊列
     */
    List<QueueDTO> getAllQueues();

    /**
     * 根據ID獲取隊列
     */
    QueueDTO getQueueById(Long id);

    /**
     * 根據球員ID獲取隊列列表
     */
    List<QueueDTO> getQueuesByPlayerId(Long playerId);

    /**
     * 根據場地ID獲取隊列列表
     */
    List<QueueDTO> getQueuesByCourtId(Long courtId);

    /**
     * 根據團隊ID和狀態獲取隊列列表
     * 
     * @param teamId 團隊ID
     * @param status 隊列狀態
     * @return 隊列列表
     */
    List<QueueDTO> getQueuesByTeamIdAndStatus(Long teamId, Queue.QueueStatus status);

    /**
     * 創建隊列記錄
     * 
     * @param playerId 球員ID
     * @param courtId 場地ID（可選）
     * @param status 隊列狀態
     * @return 創建的隊列
     */
    Queue createQueue(Long playerId, Long courtId, Queue.QueueStatus status);

    /**
     * 更新隊列狀態
     */
    QueueDTO updateQueueStatus(Long queueId, Queue.QueueStatus status);

    /**
     * 刪除隊列
     */
    void deleteQueue(Long id);

    /**
     * 刪除團隊的所有WAITING狀態的隊列
     * 
     * @param teamId 團隊ID
     * @return 刪除的隊列數量
     */
    int deleteWaitingQueuesByTeamId(Long teamId);
}

