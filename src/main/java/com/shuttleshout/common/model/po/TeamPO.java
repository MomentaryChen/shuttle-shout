package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationManyToMany;
import com.mybatisflex.annotation.RelationManyToOne;
import com.mybatisflex.annotation.RelationOneToMany;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.shuttleshout.common.model.enums.TeamLevel;
import com.shuttleshout.common.model.enums.TeamLevelTypeHandler;

/**
 * 球隊實體類
 * 
 * @author ShuttleShout Team
 */
@Table(value = "teams")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @NotBlank(message = "球隊名稱不能為空")
    @Column(value = "name")
    private String name;

    @Column(value = "description")
    private String description; // 球隊描述

    @Column(value = "color")
    private String color; // 球隊顏色標識，如 "bg-blue-500"

    @Column(value = "level", typeHandler = TeamLevelTypeHandler.class)
    private TeamLevel level; // 球隊等級

    @Min(value = 1, message = "最大人數必須大於0")
    @Column(value = "max_players")
    @Builder.Default
    private Integer maxPlayers = 20; // 球隊最大人數

    @Min(value = 1, message = "場地數量必須大於0")
    @Column(value = "court_count")
    @Builder.Default
    private Integer courtCount = 2; // 球隊分配的場地數量

    @Column(value = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(value = "user_id")
    private Long userId; // 球隊創建者/所有者用戶ID（保留用於兼容性）

    @RelationManyToOne(selfField = "userId", targetField = "id")
    private UserPO owner; // 球隊創建者/所有者用戶（保留用於兼容性）

    @RelationManyToMany(
        joinTable = "user_teams",
        joinSelfColumn = "id",
        joinTargetColumn = "user_id"
    )
    @Builder.Default
    private List<UserPO> users = new ArrayList<>(); // 球隊關聯的所有用戶

    @RelationOneToMany(selfField = "id", targetField = "teamId")
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @RelationOneToMany(selfField = "id", targetField = "teamId")
    @Builder.Default
    private List<Court> courts = new ArrayList<>();

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}
