package com.shuttleshout.common.model.po;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
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
 * 用戶實體類
 * 
 * @author ShuttleShout Team
 */
@Table(value = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPO implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @NotBlank(message = "用戶名不能為空")
    @Column(value = "username")
    private String username;

    @NotBlank(message = "密碼不能為空")
    @Column(value = "password")
    private String password; // 存儲加密後的密碼

    @Email(message = "郵箱格式不正確")
    @Column(value = "email")
    private String email;

    @Column(value = "phone_number")
    private String phoneNumber;

    @Column(value = "real_name")
    private String realName; // 真實姓名

    @Column(value = "avatar")
    private String avatar; // 頭像URL

    @Column(value = "is_active")
    private Boolean isActive = true;

    @Column(value = "last_login_at")
    private LocalDateTime lastLoginAt; // 最後登錄時間

    @RelationManyToMany(
        joinTable = "user_roles",
        joinSelfColumn = "user_id",
        joinTargetColumn = "role_id"
    )
    private List<RolePO> roles = new ArrayList<>();

    @RelationManyToMany(
        joinTable = "user_teams",
        joinSelfColumn = "user_id",
        joinTargetColumn = "id"
    )
    private List<TeamPO> teams = new ArrayList<>(); // 用戶所屬的所有球隊

    /** 羽球等級級數（1–18），null 表示未設定 */
    @Column(value = "badminton_level")
    private Integer badmintonLevel;

    @Column(value = "created_at")
    private LocalDateTime createdAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;
}

