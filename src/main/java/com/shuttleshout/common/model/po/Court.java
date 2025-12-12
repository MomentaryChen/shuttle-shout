package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

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
 * 球隊場地實體類
 * 
 * @author ShuttleShout Team
 */
@Table(value = "team_courts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Court {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @NotBlank(message = "場地名稱不能為空")
    @Column(value = "name")
    private String name;

    @Column(value = "team_id")
    private Long teamId;

    @RelationManyToOne(selfField = "teamId", targetField = "id")
    private TeamPO team;

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

    @Column(value = "match_started_at")
    private LocalDateTime matchStartedAt;

    @Column(value = "match_ended_at")
    private LocalDateTime matchEndedAt;

    @Column(value = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}
