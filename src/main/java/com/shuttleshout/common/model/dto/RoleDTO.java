package com.shuttleshout.common.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色数据传输对象
 * 
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String code;

    private String description;

    private Boolean isActive;

    private List<String> userNames = new ArrayList<>(); // 拥有该角色的用户名列表

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

