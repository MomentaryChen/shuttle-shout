package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.RelationOneToOne;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色页面资源关联实体类
 * 表示角色与页面资源的关联关系
 *
 * @author ShuttleShout Team
 */
@Table(value = "role_resource_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResourcePagePO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(value = "role_id")
    private Long roleId; // 角色ID

    @Column(value = "resource_page_id")
    private Long resourcePageId; // 页面资源ID

    @Column(value = "can_read")
    private Boolean canRead = true; // 是否可查看

    @Column(value = "can_write")
    private Boolean canWrite = false; // 是否可编辑

    @Column(value = "can_delete")
    private Boolean canDelete = false; // 是否可删除

    @RelationOneToOne(selfField = "roleId", targetField = "id")
    private RolePO role;

    @RelationOneToOne(selfField = "resourcePageId", targetField = "id")
    private ResourcePagePO resourcePage;

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}
