package com.shuttleshout.common.model.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户数据传输对象
 * 
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phoneNumber;

    private String realName;

    private String avatar;

    private Boolean isActive;

    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password; // 可选，如果提供则更新密码

    private List<Long> roleIds = new ArrayList<>(); // 角色ID列表
}

