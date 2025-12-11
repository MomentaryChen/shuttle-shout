package com.shuttleshout.common.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据传输对象
 * 
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phoneNumber;

    private String realName;

    private String avatar;

    private Boolean isActive;

    private LocalDateTime lastLoginAt;

    private List<String> roleNames = new ArrayList<>(); // 用户拥有的角色名称列表

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

