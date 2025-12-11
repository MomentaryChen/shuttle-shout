package com.shuttleshout.service;

import java.util.List;

import com.shuttleshout.common.model.dto.RoleResourcePageDTO;

/**
 * 角色页面资源关联服务接口
 *
 * @author ShuttleShout Team
 */
public interface RoleResourcePageService {

    /**
     * 获取所有角色页面资源关联
     */
    List<RoleResourcePageDTO> getAllRoleResourcePages();

    /**
     * 根据角色ID获取关联的页面资源
     */
    List<RoleResourcePageDTO> getRoleResourcePagesByRoleId(Long roleId);

    /**
     * 根据页面资源ID获取关联的角色
     */
    List<RoleResourcePageDTO> getRoleResourcePagesByResourcePageId(Long resourcePageId);

    /**
     * 为角色分配页面资源权限
     */
    void assignResourcePageToRole(Long roleId, Long resourcePageId, Boolean canRead, Boolean canWrite, Boolean canDelete);

    /**
     * 移除角色的页面资源权限
     */
    void removeResourcePageFromRole(Long roleId, Long resourcePageId);

    /**
     * 更新角色页面资源权限
     */
    void updateRoleResourcePagePermission(Long roleId, Long resourcePageId, Boolean canRead, Boolean canWrite, Boolean canDelete);
}
