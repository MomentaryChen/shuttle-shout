package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationManyToOne;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 比賽實體類
 * 記錄每場比賽的詳細信息
 * 
 * @author ShuttleShout Team
 */
@Table(value = "matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(value = "team_id")
    private Long teamId;

    @RelationManyToOne(selfField = "teamId", targetField = "id")
    private TeamPO team;

    @Column(value = "court_id")
    private Long courtId;

    @RelationManyToOne(selfField = "courtId", targetField = "id")
    private Court court;

    @Column(value = "player1_id")
    private Long player1Id;

    @RelationManyToOne(selfField = "player1Id", targetField = "id")
    private UserPO player1;

    @Column(value = "player2_id")
    private Long player2Id;

    @RelationManyToOne(selfField = "player2Id", targetField = "id")
    private UserPO player2;

    @Column(value = "player3_id")
    private Long player3Id;

    @RelationManyToOne(selfField = "player3Id", targetField = "id")
    private UserPO player3;

    @Column(value = "player4_id")
    private Long player4Id;

    @RelationManyToOne(selfField = "player4Id", targetField = "id")
    private UserPO player4;

    @Column(value = "status")
    @Builder.Default
    private MatchStatus status = MatchStatus.ONGOING;

    @Column(value = "started_at")
    private LocalDateTime startedAt;

    @Column(value = "ended_at")
    private LocalDateTime endedAt;

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 比賽狀態枚舉
     */
    public enum MatchStatus {
        ONGOING,    // 進行中
        FINISHED,   // 已完成
        CANCELLED   // 已取消
    }
}

