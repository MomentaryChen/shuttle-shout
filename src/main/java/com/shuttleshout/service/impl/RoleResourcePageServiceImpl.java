package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.RoleResourcePagePOTableDef.ROLE_RESOURCE_PAGE_PO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.exception.ErrorCode;
import com.shuttleshout.common.model.dto.RoleResourcePageDTO;
import com.shuttleshout.common.model.po.ResourcePagePO;
import com.shuttleshout.common.model.po.RolePO;
import com.shuttleshout.common.model.po.RoleResourcePagePO;
import com.shuttleshout.repository.ResourcePageRepository;
import com.shuttleshout.repository.RoleRepository;
import com.shuttleshout.repository.RoleResourcePageRepository;
import com.shuttleshout.service.RoleResourcePageService;

import lombok.RequiredArgsConstructor;

/**
 * 角色页面资源关联服务实现类
 *
 * @author ShuttleShout Team
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RoleResourcePageServiceImpl extends ServiceImpl<RoleResourcePageRepository, RoleResourcePagePO> implements RoleResourcePageService {

    private final RoleRepository roleRepository;
    private final ResourcePageRepository resourcePageRepository;

    /**
     * 获取所有角色页面资源关联
     */
    @Override
    public List<RoleResourcePageDTO> getAllRoleResourcePages() {
        List<RoleResourcePagePO> associations = getMapper().selectAll();
        return associations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据角色ID获取关联的页面资源
     */
    @Override
    public List<RoleResourcePageDTO> getRoleResourcePagesByRoleId(Long roleId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.eq(roleId));
        List<RoleResourcePagePO> associations = getMapper().selectListByQuery(queryWrapper);
        return associations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据页面资源ID获取关联的角色
     */
    @Override
    public List<RoleResourcePageDTO> getRoleResourcePagesByResourcePageId(Long resourcePageId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(resourcePageId));
        List<RoleResourcePagePO> associations = getMapper().selectListByQuery(queryWrapper);
        return associations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 为角色分配页面资源权限
     */
    @Override
    public void assignResourcePageToRole(Long roleId, Long resourcePageId, Boolean canRead, Boolean canWrite, Boolean canDelete) {
        // 检查角色是否存在
        RolePO role = roleRepository.selectOneById(roleId);
        if (role == null) {
            throw new ApiException(ErrorCode.ROLE_NOT_FOUND, "角色不存在，ID: " + roleId);
        }

        // 检查页面资源是否存在
        ResourcePagePO resourcePage = resourcePageRepository.selectOneById(resourcePageId);
        if (resourcePage == null) {
            throw new ApiException(ErrorCode.RESOURCE_PAGE_NOT_FOUND, "頁面資源不存在，ID: " + resourcePageId);
        }

        // 检查是否已存在关联
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.eq(roleId))
                .and(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(resourcePageId));
        RoleResourcePagePO existingAssociation = getMapper().selectOneByQuery(queryWrapper);
        if (existingAssociation != null) {
            throw new ApiException(ErrorCode.ROLE_RESOURCE_PAGE_ASSOCIATION_ALREADY_EXISTS);
        }

        RoleResourcePagePO association = new RoleResourcePagePO();
        association.setRoleId(roleId);
        association.setResourcePageId(resourcePageId);
        association.setCanRead(canRead != null ? canRead : true);
        association.setCanWrite(canWrite != null ? canWrite : false);
        association.setCanDelete(canDelete != null ? canDelete : false);
        association.setCreatedAt(LocalDateTime.now());
        association.setUpdatedAt(LocalDateTime.now());

        getMapper().insert(association);
    }

    /**
     * 移除角色的页面资源权限
     */
    @Override
    public void removeResourcePageFromRole(Long roleId, Long resourcePageId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.eq(roleId))
                .and(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(resourcePageId));
        RoleResourcePagePO association = getMapper().selectOneByQuery(queryWrapper);
        if (association == null) {
            throw new ApiException(ErrorCode.ROLE_RESOURCE_PAGE_ASSOCIATION_NOT_FOUND);
        }

        getMapper().deleteById(association.getId());
    }

    /**
     * 更新角色页面资源权限
     */
    @Override
    public void updateRoleResourcePagePermission(Long roleId, Long resourcePageId, Boolean canRead, Boolean canWrite, Boolean canDelete) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.eq(roleId))
                .and(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(resourcePageId));
        RoleResourcePagePO association = getMapper().selectOneByQuery(queryWrapper);
        if (association == null) {
            throw new ApiException(ErrorCode.ROLE_RESOURCE_PAGE_ASSOCIATION_NOT_FOUND);
        }

        if (canRead != null) {
            association.setCanRead(canRead);
        }
        if (canWrite != null) {
            association.setCanWrite(canWrite);
        }
        if (canDelete != null) {
            association.setCanDelete(canDelete);
        }
        association.setUpdatedAt(LocalDateTime.now());

        getMapper().update(association);
    }

    /**
     * 转换为DTO
     */
    private RoleResourcePageDTO convertToDto(RoleResourcePagePO association) {
        RoleResourcePageDTO dto = new RoleResourcePageDTO();
        dto.setId(association.getId());
        dto.setRoleId(association.getRoleId());
        dto.setResourcePageId(association.getResourcePageId());
        dto.setCanRead(association.getCanRead());
        dto.setCanWrite(association.getCanWrite());
        dto.setCanDelete(association.getCanDelete());
        dto.setCreatedAt(association.getCreatedAt());
        dto.setUpdatedAt(association.getUpdatedAt());

        // 设置角色和页面资源名称
        if (association.getRole() != null) {
            dto.setRoleName(association.getRole().getName());
        }
        if (association.getResourcePage() != null) {
            dto.setResourcePageName(association.getResourcePage().getName());
            dto.setResourcePageCode(association.getResourcePage().getCode());
        }

        return dto;
    }
}
