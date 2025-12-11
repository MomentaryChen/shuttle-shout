package com.shuttleshout.common.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 页面资源更新数据传输对象
 *
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePageUpdateDTO {

    private String name;

    private String code;

    private String path;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Long parentId;

    private Boolean isActive;

    // 关联的角色ID列表
    private List<Long> roleIds;
}
