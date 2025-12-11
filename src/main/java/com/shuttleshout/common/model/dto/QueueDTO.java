package com.shuttleshout.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.shuttleshout.common.model.po.Queue.QueueStatus;

/**
 * 队列数据传输对象
 * 
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueDTO {

    private Long id;

    private Long playerId;

    private String playerName;

    private Long courtId;

    private String courtName;

    private QueueStatus status;

    private Integer queueNumber;

    private LocalDateTime calledAt;

    private LocalDateTime servedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
