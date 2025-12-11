package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationManyToMany;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色实体类
 * 
 * @author ShuttleShout Team
 */
@Table(value = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Column(value = "name")
    private String name;

    @Column(value = "code")
    private String code; // 角色代码，如 ADMIN, USER, MANAGER

    @Column(value = "description")
    private String description; // 角色描述

    @Column(value = "is_active")
    private Boolean isActive = true;

    @RelationManyToMany(
        joinTable = "user_roles",
        joinSelfColumn = "role_id",
        joinTargetColumn = "id"
    )
    private List<UserPO> users = new ArrayList<>();

    @RelationManyToMany(
        joinTable = "role_resource_pages",
        joinSelfColumn = "role_id",
        joinTargetColumn = "id"
    )
    private List<ResourcePagePO> resourcePages = new ArrayList<>();

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}

