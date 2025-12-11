package com.shuttleshout.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.annotation.CurrentUserId;
import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.ResourcePageCreateDTO;
import com.shuttleshout.common.model.dto.ResourcePageDTO;
import com.shuttleshout.common.model.dto.ResourcePageUpdateDTO;
import com.shuttleshout.service.ResourcePageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 页面资源控制器
 *
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/resource-pages")
@Tag(name = "页面资源管理", description = "页面资源相关的API接口")
@RequiredArgsConstructor
public class ResourcePageController {

    private final ResourcePageService resourcePageService;

    /**
     * 获取所有页面资源
     */
    @GetMapping
    // @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取所有页面资源", description = "返回系统中所有页面资源的列表")
    public ResponseEntity<List<ResourcePageDTO>> getAllResourcePages() {
        try {
            List<ResourcePageDTO> resourcePages = resourcePageService.getAllResourcePages();
            return ResponseEntity.ok(resourcePages);
        } catch (Exception e) {
            throw new ApiException("获取页面资源列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_RESOURCE_PAGES_ERROR", e);
        }
    }

    /**
     * 根据ID获取页面资源
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "根据ID获取页面资源", description = "根据页面资源ID获取详细信息")
    public ResponseEntity<ResourcePageDTO> getResourcePageById(@PathVariable Long id) {
        try {
            ResourcePageDTO resourcePage = resourcePageService.getResourcePageById(id);
            return ResponseEntity.ok(resourcePage);
        } catch (Exception e) {
            throw new ApiException("获取页面资源失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 根据代码获取页面资源
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "根据代码获取页面资源", description = "根据页面资源代码获取详细信息")
    public ResponseEntity<ResourcePageDTO> getResourcePageByCode(@PathVariable String code) {
        try {
            ResourcePageDTO resourcePage = resourcePageService.getResourcePageByCode(code);
            return ResponseEntity.ok(resourcePage);
        } catch (Exception e) {
            throw new ApiException("获取页面资源失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 根据角色ID获取页面资源
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "根据角色获取页面资源", description = "获取指定角色可访问的所有页面资源")
    public ResponseEntity<List<ResourcePageDTO>> getResourcePagesByRoleId(@PathVariable Long roleId) {
        try {
            List<ResourcePageDTO> resourcePages = resourcePageService.getResourcePagesByRoleId(roleId);
            return ResponseEntity.ok(resourcePages);
        } catch (Exception e) {
            throw new ApiException("获取角色页面资源失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_ROLE_RESOURCE_PAGES_ERROR", e);
        }
    }

    /**
     * 获取当前用户可访问的页面资源
     */
    @GetMapping("/my-accessible")
    @Operation(summary = "获取我的页面资源", description = "获取当前登录用户可访问的所有页面资源")
    public ResponseEntity<List<ResourcePageDTO>> getMyAccessibleResourcePages(@CurrentUserId Long userId) {
        try {
            List<ResourcePageDTO> resourcePages = resourcePageService.getResourcePagesByUserId(userId);
            return ResponseEntity.ok(resourcePages);
        } catch (Exception e) {
            throw new ApiException("获取我的页面资源失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_MY_RESOURCE_PAGES_ERROR", e);
        }
    }

    /**
     * 创建页面资源
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建页面资源", description = "创建新的页面资源")
    public ResponseEntity<ResourcePageDTO> createResourcePage(@Valid @RequestBody ResourcePageCreateDTO resourcePageCreateDto) {
        try {
            ResourcePageDTO createdResourcePage = resourcePageService.createResourcePage(resourcePageCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdResourcePage);
        } catch (Exception e) {
            throw new ApiException("创建页面资源失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "CREATE_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 更新页面资源
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新页面资源", description = "更新指定页面资源的信息")
    public ResponseEntity<ResourcePageDTO> updateResourcePage(@PathVariable Long id, @Valid @RequestBody ResourcePageUpdateDTO resourcePageUpdateDto) {
        try {
            ResourcePageDTO updatedResourcePage = resourcePageService.updateResourcePage(id, resourcePageUpdateDto);
            return ResponseEntity.ok(updatedResourcePage);
        } catch (Exception e) {
            throw new ApiException("更新页面资源失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "UPDATE_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 删除页面资源
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除页面资源", description = "删除指定的页面资源")
    public ResponseEntity<Void> deleteResourcePage(@PathVariable Long id) {
        try {
            resourcePageService.deleteResourcePage(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("删除页面资源失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "DELETE_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 检查用户页面权限
     */
    @GetMapping("/check-permission/{resourcePageCode}/{permission}")
    @Operation(summary = "检查页面权限", description = "检查当前用户是否有指定页面的权限")
    public ResponseEntity<Boolean> checkPermission(@CurrentUserId Long userId, @PathVariable String resourcePageCode, @PathVariable String permission) {
        try {
            boolean hasPermission = resourcePageService.hasPermission(userId, resourcePageCode, permission);
            return ResponseEntity.ok(hasPermission);
        } catch (Exception e) {
            throw new ApiException("检查页面权限失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "CHECK_PERMISSION_ERROR", e);
        }
    }
}
