package com.shuttleshout.common.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户团队关系数据传输对象
 *
 * @author ShuttleShout Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTeamDTO {

    private Long id;
    private Long userId;
    private Long teamId;
    private Boolean isOwner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联信息
    private String userName;
    private String userRealName;
    private String userEmail;
    private String teamName;
    private String teamColor;
}
