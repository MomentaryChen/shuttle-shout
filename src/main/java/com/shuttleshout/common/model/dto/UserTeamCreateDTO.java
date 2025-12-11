package com.shuttleshout.common.model.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户团队关系创建数据传输对象
 *
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTeamCreateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "团队ID不能为空")
    private Long teamId;

    private Boolean isOwner = false; // 是否为团队所有者
}
