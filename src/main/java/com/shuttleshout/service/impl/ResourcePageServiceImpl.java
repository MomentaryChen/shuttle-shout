package com.shuttleshout.service.impl;

import static com.shuttleshout.common.model.po.table.ResourcePagePOTableDef.RESOURCE_PAGE_PO;
import static com.shuttleshout.common.model.po.table.RoleResourcePagePOTableDef.ROLE_RESOURCE_PAGE_PO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.http.HttpStatus;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.ResourcePageCreateDTO;
import com.shuttleshout.common.model.dto.ResourcePageDTO;
import com.shuttleshout.common.model.dto.ResourcePageUpdateDTO;
import com.shuttleshout.common.model.po.ResourcePagePO;
import com.shuttleshout.common.model.po.RolePO;
import com.shuttleshout.common.model.po.RoleResourcePagePO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.repository.ResourcePageRepository;
import com.shuttleshout.repository.RoleRepository;
import com.shuttleshout.repository.RoleResourcePageRepository;
import com.shuttleshout.repository.UserRepository;
import com.shuttleshout.service.ResourcePageService;

import lombok.RequiredArgsConstructor;

/**
 * 頁面資源服務實現類
 *
 * @author ShuttleShout Team
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ResourcePageServiceImpl extends ServiceImpl<ResourcePageRepository, ResourcePagePO> implements ResourcePageService {

    private final RoleResourcePageRepository roleResourcePageRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    /**
     * 獲取所有頁面資源
     */
    @Override
    public List<ResourcePageDTO> getAllResourcePages() {
        List<ResourcePagePO> resourcePages = getMapper().selectAll();
        return resourcePages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據ID獲取頁面資源
     */
    @Override
    public ResourcePageDTO getResourcePageById(Long id) {
        ResourcePagePO resourcePage = getMapper().selectOneById(id);
        if (resourcePage == null) {
            throw new ApiException("頁面資源不存在，ID: " + id, HttpStatus.NOT_FOUND, "RESOURCE_PAGE_NOT_FOUND");
        }
        return convertToDto(resourcePage);
    }

    /**
     * 根據代碼獲取頁面資源
     */
    @Override
    public ResourcePageDTO getResourcePageByCode(String code) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(RESOURCE_PAGE_PO.CODE.eq(code));
        ResourcePagePO resourcePage = getMapper().selectOneByQuery(queryWrapper);
        if (resourcePage == null) {
            throw new ApiException("頁面資源不存在，代碼: " + code, HttpStatus.NOT_FOUND, "RESOURCE_PAGE_NOT_FOUND");
        }
        return convertToDto(resourcePage);
    }

    /**
     * 根據角色ID獲取該角色可訪問的所有頁面資源
     */
    @Override
    public List<ResourcePageDTO> getResourcePagesByRoleId(Long roleId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.eq(roleId));
        List<RoleResourcePagePO> roleResourcePages = roleResourcePageRepository.selectListByQuery(queryWrapper);

        return roleResourcePages.stream()
                .map(roleResourcePage -> convertToDto(roleResourcePage.getResourcePage()))
                .collect(Collectors.toList());
    }

    /**
     * 根據用戶ID獲取該用戶可訪問的所有頁面資源
     */
    @Override
    public List<ResourcePageDTO> getResourcePagesByUserId(Long userId) {
        // 獲取用戶的所有角色（包含關聯的roles數據）
        UserPO user = userRepository.selectOneWithRelationsById(userId);
        if (user == null || CollectionUtils.isEmpty(user.getRoles())) {
            return new ArrayList<>();
        }

        // 獲取所有角色ID
        List<Long> roleIds = user.getRoles().stream()
                .map(RolePO::getId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 獲取這些角色可訪問的所有頁面資源（去重）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.in(roleIds));
        List<RoleResourcePagePO> roleResourcePages = roleResourcePageRepository.selectListWithRelationsByQuery(queryWrapper);

        if(roleResourcePages.isEmpty()) {
            return new ArrayList<>();
        }

        return roleResourcePages.stream()
                .map(page -> convertToDto(page.getResourcePage()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 創建頁面資源
     */
    @Override
    public ResourcePageDTO createResourcePage(@Valid ResourcePageCreateDTO resourcePageCreateDto) {
        // 檢查代碼是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(RESOURCE_PAGE_PO.CODE.eq(resourcePageCreateDto.getCode()));
        ResourcePagePO existingResourcePage = getMapper().selectOneByQuery(queryWrapper);
        if (existingResourcePage != null) {
            throw new ApiException("頁面資源代碼已存在: " + resourcePageCreateDto.getCode(), HttpStatus.BAD_REQUEST, "RESOURCE_PAGE_CODE_ALREADY_EXISTS");
        }

        ResourcePagePO resourcePage = new ResourcePagePO();
        resourcePage.setName(resourcePageCreateDto.getName());
        resourcePage.setCode(resourcePageCreateDto.getCode());
        resourcePage.setPath(resourcePageCreateDto.getPath());
        resourcePage.setDescription(resourcePageCreateDto.getDescription());
        resourcePage.setIcon(resourcePageCreateDto.getIcon());
        resourcePage.setSortOrder(resourcePageCreateDto.getSortOrder());
        resourcePage.setParentId(resourcePageCreateDto.getParentId());
        resourcePage.setIsActive(true);
        resourcePage.setCreatedAt(LocalDateTime.now());
        resourcePage.setUpdatedAt(LocalDateTime.now());

        getMapper().insert(resourcePage);

        // 分配角色
        if (resourcePageCreateDto.getRoleIds() != null && !resourcePageCreateDto.getRoleIds().isEmpty()) {
            assignRolesToResourcePage(resourcePage.getId(), resourcePageCreateDto.getRoleIds());
        }

        return convertToDto(resourcePage);
    }

    /**
     * 更新頁面資源
     */
    @Override
    public ResourcePageDTO updateResourcePage(Long id, @Valid ResourcePageUpdateDTO resourcePageUpdateDto) {
        ResourcePagePO resourcePage = getMapper().selectOneById(id);
        if (resourcePage == null) {
            throw new ApiException("頁面資源不存在，ID: " + id, HttpStatus.NOT_FOUND, "RESOURCE_PAGE_NOT_FOUND");
        }

        // 檢查代碼是否已被其他資源使用
        if (resourcePageUpdateDto.getCode() != null && !resourcePageUpdateDto.getCode().equals(resourcePage.getCode())) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(RESOURCE_PAGE_PO.CODE.eq(resourcePageUpdateDto.getCode()))
                    .and(RESOURCE_PAGE_PO.ID.ne(id));
            ResourcePagePO existingResourcePage = getMapper().selectOneByQuery(queryWrapper);
            if (existingResourcePage != null) {
                throw new ApiException("頁面資源代碼已被使用: " + resourcePageUpdateDto.getCode(), HttpStatus.BAD_REQUEST, "RESOURCE_PAGE_CODE_ALREADY_EXISTS");
            }
        }

        if (resourcePageUpdateDto.getName() != null) {
            resourcePage.setName(resourcePageUpdateDto.getName());
        }
        if (resourcePageUpdateDto.getCode() != null) {
            resourcePage.setCode(resourcePageUpdateDto.getCode());
        }
        if (resourcePageUpdateDto.getPath() != null) {
            resourcePage.setPath(resourcePageUpdateDto.getPath());
        }
        if (resourcePageUpdateDto.getDescription() != null) {
            resourcePage.setDescription(resourcePageUpdateDto.getDescription());
        }
        if (resourcePageUpdateDto.getIcon() != null) {
            resourcePage.setIcon(resourcePageUpdateDto.getIcon());
        }
        if (resourcePageUpdateDto.getSortOrder() != null) {
            resourcePage.setSortOrder(resourcePageUpdateDto.getSortOrder());
        }
        if (resourcePageUpdateDto.getParentId() != null) {
            resourcePage.setParentId(resourcePageUpdateDto.getParentId());
        }
        if (resourcePageUpdateDto.getIsActive() != null) {
            resourcePage.setIsActive(resourcePageUpdateDto.getIsActive());
        }
        resourcePage.setUpdatedAt(LocalDateTime.now());

        getMapper().update(resourcePage);

        // 更新角色關聯
        if (resourcePageUpdateDto.getRoleIds() != null) {
            assignRolesToResourcePage(resourcePage.getId(), resourcePageUpdateDto.getRoleIds());
        }

        return convertToDto(resourcePage);
    }

    /**
     * 刪除頁面資源
     */
    @Override
    public void deleteResourcePage(Long id) {
        ResourcePagePO resourcePage = getMapper().selectOneById(id);
        if (resourcePage == null) {
            throw new ApiException("頁面資源不存在，ID: " + id, HttpStatus.NOT_FOUND, "RESOURCE_PAGE_NOT_FOUND");
        }

        // 刪除角色關聯
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(id));
        List<RoleResourcePagePO> roleResourcePages = roleResourcePageRepository.selectListByQuery(queryWrapper);
        for (RoleResourcePagePO roleResourcePage : roleResourcePages) {
            roleResourcePageRepository.deleteById(roleResourcePage.getId());
        }

        getMapper().deleteById(id);
    }

    /**
     * 為頁面資源分配角色
     */
    @Override
    public void assignRolesToResourcePage(Long resourcePageId, List<Long> roleIds) {
        // 先刪除現有關聯 - 使用條件刪除避免ID類型問題
        roleResourcePageRepository.deleteByQuery(
            QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.eq(resourcePageId))
        );

        // 添加新關聯
        for (Long roleId : roleIds) {
            RolePO role = roleRepository.selectOneById(roleId);
            if (role == null) {
                throw new ApiException("角色不存在，ID: " + roleId, HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND");
            }

            RoleResourcePagePO association = new RoleResourcePagePO();
            association.setRoleId(roleId);
            association.setResourcePageId(resourcePageId);
            association.setCanRead(true); // 默認可讀
            association.setCanWrite(false); // 默認不可寫
            association.setCanDelete(false); // 默認不可刪除
            association.setCreatedAt(LocalDateTime.now());
            association.setUpdatedAt(LocalDateTime.now());

            roleResourcePageRepository.insert(association);
        }
    }

    /**
     * 檢查用戶是否有權限訪問指定頁面
     */
    @Override
    public boolean hasPermission(Long userId, String resourcePageCode, String permission) {
        // 獲取用戶的所有角色
        UserPO user = userRepository.selectOneById(userId);
        if (user == null || user.getRoles() == null) {
            return false;
        }

        // 獲取所有角色ID
        List<Long> roleIds = user.getRoles().stream()
                .map(RolePO::getId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return false;
        }

        // 檢查是否有權限
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(ROLE_RESOURCE_PAGE_PO.ROLE_ID.in(roleIds))
                .and(ROLE_RESOURCE_PAGE_PO.RESOURCE_PAGE_ID.in(
                    QueryWrapper.create()
                        .select(RESOURCE_PAGE_PO.ID)
                        .from(RESOURCE_PAGE_PO)
                        .where(RESOURCE_PAGE_PO.CODE.eq(resourcePageCode))
                ));

        List<RoleResourcePagePO> permissions = roleResourcePageRepository.selectListByQuery(queryWrapper);

        return permissions.stream().anyMatch(p -> {
            switch (permission.toLowerCase()) {
                case "read":
                    return Boolean.TRUE.equals(p.getCanRead());
                case "write":
                    return Boolean.TRUE.equals(p.getCanWrite());
                case "delete":
                    return Boolean.TRUE.equals(p.getCanDelete());
                default:
                    return false;
            }
        });
    }

    /**
     * 轉換為DTO
     */
    private ResourcePageDTO convertToDto(ResourcePagePO resourcePage) {
        ResourcePageDTO dto = new ResourcePageDTO();
        dto.setId(resourcePage.getId());
        dto.setName(resourcePage.getName());
        dto.setCode(resourcePage.getCode());
        dto.setPath(resourcePage.getPath());
        dto.setDescription(resourcePage.getDescription());
        dto.setIcon(resourcePage.getIcon());
        dto.setSortOrder(resourcePage.getSortOrder());
        dto.setParentId(resourcePage.getParentId());
        dto.setIsActive(resourcePage.getIsActive());
        dto.setCreatedAt(resourcePage.getCreatedAt());
        dto.setUpdatedAt(resourcePage.getUpdatedAt());

        // 獲取關聯的角色名稱列表
        if (resourcePage.getRoles() != null && !resourcePage.getRoles().isEmpty()) {
            List<String> roleNames = resourcePage.getRoles().stream()
                    .map(RolePO::getName)
                    .collect(Collectors.toList());
            dto.setRoleNames(roleNames);
        }

        return dto;
    }
}
