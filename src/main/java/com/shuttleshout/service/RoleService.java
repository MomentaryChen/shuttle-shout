package com.shuttleshout.service;

import java.util.List;

import javax.validation.Valid;

import com.mybatisflex.core.service.IService;
import com.shuttleshout.common.model.dto.RoleDTO;
import com.shuttleshout.common.model.po.RolePO;

/**
 * 角色服务接口
 * 
 * @author ShuttleShout Team
 */
public interface RoleService extends IService<RolePO> {

    /**
     * 获取所有角色
     */
    List<RoleDTO> getAllRoles();

    /**
     * 根据ID获取角色
     */
    RoleDTO getRoleById(Long id);

    /**
     * 根据代码获取角色
     */
    RoleDTO getRoleByCode(String code);

    /**
     * 创建角色
     */
    RoleDTO createRole(@Valid RoleDTO roleDto);

    /**
     * 更新角色
     */
    RoleDTO updateRole(Long id, @Valid RoleDTO roleDto);

    /**
     * 删除角色
     */
    void deleteRole(Long id);
}
