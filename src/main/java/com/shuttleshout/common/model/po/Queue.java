package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationManyToOne;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 叫号队列实体类
 * 
 * @author ShuttleShout Team
 */
@Table(value = "queues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Queue {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(value = "player_id")
    private Long playerId;

    @RelationManyToOne(selfField = "playerId", targetField = "id")
    private Player player;

    @Column(value = "court_id")
    private Long courtId;

    @RelationManyToOne(selfField = "courtId", targetField = "id")
    private Court court;

    @Column(value = "status")
    private QueueStatus status = QueueStatus.WAITING;

    @Column(value = "queue_number")
    private Integer queueNumber;

    @Column(value = "called_at")
    private LocalDateTime calledAt;

    @Column(value = "served_at")
    private LocalDateTime servedAt;

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 队列状态枚举
     */
    public enum QueueStatus {
        WAITING,    // 等待中
        CALLED,     // 已叫号
        SERVED,     // 已服务
        CANCELLED   // 已取消
    }
}
