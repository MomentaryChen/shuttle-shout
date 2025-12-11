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
 * 页面资源实体类
 * 表示系统中的页面资源，用于角色权限控制
 *
 * @author ShuttleShout Team
 */
@Table(value = "resource_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePagePO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @NotBlank(message = "页面名称不能为空")
    @Column(value = "name")
    private String name; // 页面显示名称

    @NotBlank(message = "页面代码不能为空")
    @Column(value = "code")
    private String code; // 页面代码标识符，如 "PERSONNEL_MANAGEMENT", "TEAM_MANAGEMENT"

    @Column(value = "path")
    private String path; // 页面路由路径，如 "/personnel-management"

    @Column(value = "description")
    private String description; // 页面描述

    @Column(value = "icon")
    private String icon; // 页面图标

    @Column(value = "sort_order")
    private Integer sortOrder = 0; // 排序顺序

    @Column(value = "parent_id")
    private Long parentId; // 父级页面ID，用于构建页面层级

    @Column(value = "is_active")
    private Boolean isActive = true;

    @RelationManyToMany(
        joinTable = "role_resource_pages",
        joinSelfColumn = "resource_page_id",
        joinTargetColumn = "role_id"
    )
    private List<RolePO> roles = new ArrayList<>();

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}
