package com.shuttleshout.common.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色页面资源关联数据传输对象
 *
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResourcePageDTO {

    private Long id;

    private Long roleId;

    private Long resourcePageId;

    private String roleName;

    private String resourcePageName;

    private String resourcePageCode;

    private Boolean canRead = true;

    private Boolean canWrite = false;

    private Boolean canDelete = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
