package com.shuttleshout.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.RoleResourcePageDTO;
import com.shuttleshout.service.RoleResourcePageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 角色页面资源关联控制器
 *
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/role-resource-pages")
@Tag(name = "角色页面权限管理", description = "角色与页面资源权限关联的API接口")
@RequiredArgsConstructor
public class RoleResourcePageController {

    private final RoleResourcePageService roleResourcePageService;

    /**
     * 获取所有角色页面资源关联
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取所有角色页面关联", description = "返回所有角色与页面资源的关联信息")
    public ResponseEntity<List<RoleResourcePageDTO>> getAllRoleResourcePages() {
        try {
            List<RoleResourcePageDTO> associations = roleResourcePageService.getAllRoleResourcePages();
            return ResponseEntity.ok(associations);
        } catch (Exception e) {
            throw new ApiException("獲取角色頁面關聯列表失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_ROLE_RESOURCE_PAGES_ERROR", e);
        }
    }

    /**
     * 根据角色ID获取关联的页面资源
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色的页面权限", description = "获取指定角色拥有的所有页面资源权限")
    public ResponseEntity<List<RoleResourcePageDTO>> getRoleResourcePagesByRoleId(@PathVariable Long roleId) {
        try {
            List<RoleResourcePageDTO> associations = roleResourcePageService.getRoleResourcePagesByRoleId(roleId);
            return ResponseEntity.ok(associations);
        } catch (Exception e) {
            throw new ApiException("獲取角色頁面權限失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_ROLE_RESOURCE_PAGES_ERROR", e);
        }
    }

    /**
     * 根据页面资源ID获取关联的角色
     */
    @GetMapping("/resource-page/{resourcePageId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取页面资源的角色权限", description = "获取有权限访问指定页面资源的所有角色")
    public ResponseEntity<List<RoleResourcePageDTO>> getRoleResourcePagesByResourcePageId(@PathVariable Long resourcePageId) {
        try {
            List<RoleResourcePageDTO> associations = roleResourcePageService.getRoleResourcePagesByResourcePageId(resourcePageId);
            return ResponseEntity.ok(associations);
        } catch (Exception e) {
            throw new ApiException("獲取頁面資源的角色權限失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_RESOURCE_PAGE_ROLES_ERROR", e);
        }
    }

    /**
     * 为角色分配页面资源权限
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "分配页面权限", description = "为指定角色分配页面资源的访问权限")
    public ResponseEntity<Void> assignResourcePageToRole(
            @RequestParam Long roleId,
            @RequestParam Long resourcePageId,
            @RequestParam(defaultValue = "true") Boolean canRead,
            @RequestParam(defaultValue = "false") Boolean canWrite,
            @RequestParam(defaultValue = "false") Boolean canDelete) {
        try {
            roleResourcePageService.assignResourcePageToRole(roleId, resourcePageId, canRead, canWrite, canDelete);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            throw new ApiException("分配頁面權限失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "ASSIGN_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 移除角色的页面资源权限
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "移除页面权限", description = "移除指定角色的页面资源访问权限")
    public ResponseEntity<Void> removeResourcePageFromRole(
            @RequestParam Long roleId,
            @RequestParam Long resourcePageId) {
        try {
            roleResourcePageService.removeResourcePageFromRole(roleId, resourcePageId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("移除頁面權限失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "REMOVE_RESOURCE_PAGE_ERROR", e);
        }
    }

    /**
     * 更新角色页面资源权限
     */
    @PutMapping("/update-permission")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新页面权限", description = "更新指定角色对页面资源的权限设置")
    public ResponseEntity<Void> updateRoleResourcePagePermission(
            @RequestParam Long roleId,
            @RequestParam Long resourcePageId,
            @RequestParam(required = false) Boolean canRead,
            @RequestParam(required = false) Boolean canWrite,
            @RequestParam(required = false) Boolean canDelete) {
        try {
            roleResourcePageService.updateRoleResourcePagePermission(roleId, resourcePageId, canRead, canWrite, canDelete);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ApiException("更新頁面權限失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "UPDATE_RESOURCE_PAGE_PERMISSION_ERROR", e);
        }
    }
}
