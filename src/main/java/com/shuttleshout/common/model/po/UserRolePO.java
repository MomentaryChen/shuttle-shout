package com.shuttleshout.common.model.po;

import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户角色关联表实体类
 * 
 * @author ShuttleShout Team
 */
@Table(value = "user_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRolePO {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(value = "user_id")
    private Long userId;

    @Column(value = "role_id")
    private Long roleId;

    @Column(value = "created_at")
    private LocalDateTime createdAt;
}

