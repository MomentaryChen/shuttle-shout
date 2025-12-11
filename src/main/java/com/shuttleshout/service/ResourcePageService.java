package com.shuttleshout.service;

import java.util.List;

import javax.validation.Valid;

import com.shuttleshout.common.model.dto.ResourcePageCreateDTO;
import com.shuttleshout.common.model.dto.ResourcePageDTO;
import com.shuttleshout.common.model.dto.ResourcePageUpdateDTO;

/**
 * 页面资源服务接口
 *
 * @author ShuttleShout Team
 */
public interface ResourcePageService {

    /**
     * 获取所有页面资源
     */
    List<ResourcePageDTO> getAllResourcePages();

    /**
     * 根据ID获取页面资源
     */
    ResourcePageDTO getResourcePageById(Long id);

    /**
     * 根据代码获取页面资源
     */
    ResourcePageDTO getResourcePageByCode(String code);

    /**
     * 根据角色ID获取该角色可访问的所有页面资源
     */
    List<ResourcePageDTO> getResourcePagesByRoleId(Long roleId);

    /**
     * 根据用户ID获取该用户可访问的所有页面资源
     */
    List<ResourcePageDTO> getResourcePagesByUserId(Long userId);

    /**
     * 创建页面资源
     */
    ResourcePageDTO createResourcePage(@Valid ResourcePageCreateDTO resourcePageCreateDto);

    /**
     * 更新页面资源
     */
    ResourcePageDTO updateResourcePage(Long id, @Valid ResourcePageUpdateDTO resourcePageUpdateDto);

    /**
     * 删除页面资源
     */
    void deleteResourcePage(Long id);

    /**
     * 为页面资源分配角色
     */
    void assignRolesToResourcePage(Long resourcePageId, List<Long> roleIds);

    /**
     * 检查用户是否有权限访问指定页面
     */
    boolean hasPermission(Long userId, String resourcePageCode, String permission);
}
