package com.shuttleshout.common.model.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 页面资源创建数据传输对象
 *
 * @author ShuttleShout Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePageCreateDTO {

    @NotBlank(message = "页面名称不能为空")
    private String name;

    @NotBlank(message = "页面代码不能为空")
    private String code;

    private String path;

    private String description;

    private String icon;

    private Integer sortOrder = 0;

    private Long parentId;

    // 关联的角色ID列表
    private List<Long> roleIds;
}
