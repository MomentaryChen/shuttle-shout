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
 * 球员实体类
 * 
 * @author ShuttleShout Team
 */
@Table(value = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @NotBlank(message = "球员姓名不能为空")
    @Column(value = "name")
    private String name;

    @Column(value = "phone_number")
    private String phoneNumber;

    @Column(value = "notes")
    private String notes;

    @Column(value = "team_id")
    private Long teamId;

    @RelationManyToOne(selfField = "teamId", targetField = "id")
    private TeamPO team;

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}
