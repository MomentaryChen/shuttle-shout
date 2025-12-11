package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 球队用户关联表实体类
 * 用于实现 Team 和 User 的多对多关系
 * 
 * @author ShuttleShout Team
 */
@Table(value = "user_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTeamPO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(value = "team_id")
    private Long teamId;

    @Column(value = "user_id")
    private Long userId;

    @Column(value = "is_owner")
    private Boolean isOwner = false; // 是否为球队创建者/所有者

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}

