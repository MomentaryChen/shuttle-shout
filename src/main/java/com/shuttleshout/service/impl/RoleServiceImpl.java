package com.shuttleshout.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.RoleDTO;
import com.shuttleshout.common.model.po.RolePO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.repository.RoleRepository;
import com.shuttleshout.service.RoleService;

import static com.shuttleshout.common.model.po.table.RolePOTableDef.ROLE_PO;

/**
 * 角色服务实现类
 * 
 * @author ShuttleShout Team
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleRepository, RolePO> implements RoleService {

    /**
     * 获取所有角色
     */
    @Override
    public List<RoleDTO> getAllRoles() {
        List<RolePO> roles = getMapper().selectAll();
        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取角色
     */
    @Override
    public RoleDTO getRoleById(Long id) {
        RolePO role = getMapper().selectOneById(id);
        if (role == null) {
            throw new ApiException(ErrorCode.ROLE_NOT_FOUND, "角色不存在，ID: " + id);
        }
        return convertToDto(role);
    }

    /**
     * 根据代码获取角色
     */
    @Override
    public RoleDTO getRoleByCode(String code) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_PO.CODE.eq(code));
        RolePO role = getMapper().selectOneByQuery(queryWrapper);
        if (role == null) {
            throw new ApiException(ErrorCode.ROLE_NOT_FOUND, "角色不存在，代碼: " + code);
        }
        return convertToDto(role);
    }

    /**
     * 创建角色
     */
    @Override
    public RoleDTO createRole(@Valid RoleDTO roleDto) {
        // 检查代码是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_PO.CODE.eq(roleDto.getCode()));
        RolePO existingRole = getMapper().selectOneByQuery(queryWrapper);
        if (existingRole != null) {
            throw new ApiException(ErrorCode.ROLE_CODE_ALREADY_EXISTS, "角色代碼已存在: " + roleDto.getCode());
        }

        RolePO role = new RolePO();
        role.setName(roleDto.getName());
        role.setCode(roleDto.getCode());
        role.setDescription(roleDto.getDescription());
        role.setIsActive(roleDto.getIsActive() != null ? roleDto.getIsActive() : true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        getMapper().insert(role);
        return convertToDto(role);
    }

    /**
     * 更新角色
     */
    @Override
    public RoleDTO updateRole(Long id, @Valid RoleDTO roleDto) {
        RolePO role = getMapper().selectOneById(id);
        if (role == null) {
            throw new ApiException(ErrorCode.ROLE_NOT_FOUND, "角色不存在，ID: " + id);
        }

        // 如果代码改变，检查新代码是否已存在
        if (roleDto.getCode() != null && !roleDto.getCode().equals(role.getCode())) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(ROLE_PO.CODE.eq(roleDto.getCode()))
                    .and(ROLE_PO.ID.ne(id));
            RolePO existingRole = getMapper().selectOneByQuery(queryWrapper);
            if (existingRole != null) {
                throw new ApiException(ErrorCode.ROLE_CODE_ALREADY_EXISTS, "角色代碼已存在: " + roleDto.getCode());
            }
        }

        if (roleDto.getName() != null) {
            role.setName(roleDto.getName());
        }
        if (roleDto.getCode() != null) {
            role.setCode(roleDto.getCode());
        }
        if (roleDto.getDescription() != null) {
            role.setDescription(roleDto.getDescription());
        }
        if (roleDto.getIsActive() != null) {
            role.setIsActive(roleDto.getIsActive());
        }
        role.setUpdatedAt(LocalDateTime.now());

        getMapper().update(role);
        return convertToDto(role);
    }

    /**
     * 删除角色
     */
    @Override
    public void deleteRole(Long id) {
        RolePO role = getMapper().selectOneById(id);
        if (role == null) {
            throw new ApiException(ErrorCode.ROLE_NOT_FOUND, "角色不存在，ID: " + id);
        }
        getMapper().deleteById(id);
    }

    /**
     * 转换为DTO
     */
    private RoleDTO convertToDto(RolePO role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setDescription(role.getDescription());
        dto.setIsActive(role.getIsActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());

        // 获取拥有该角色的用户名列表
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            List<String> userNames = role.getUsers().stream()
                    .map(UserPO::getUsername)
                    .collect(Collectors.toList());
            dto.setUserNames(userNames);
        }

        return dto;
    }
}
