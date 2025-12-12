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
        List<ResourcePageDTO> resourcePages = resourcePageService.getAllResourcePages();
        return ResponseEntity.ok(resourcePages);
    }

    /**
     * 根据ID获取页面资源
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "根据ID获取页面资源", description = "根据页面资源ID获取详细信息")
    public ResponseEntity<ResourcePageDTO> getResourcePageById(@PathVariable Long id) {
        ResourcePageDTO resourcePage = resourcePageService.getResourcePageById(id);
        return ResponseEntity.ok(resourcePage);
    }

    /**
     * 根据代码获取页面资源
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "根据代码获取页面资源", description = "根据页面资源代码获取详细信息")
    public ResponseEntity<ResourcePageDTO> getResourcePageByCode(@PathVariable String code) {
        ResourcePageDTO resourcePage = resourcePageService.getResourcePageByCode(code);
        return ResponseEntity.ok(resourcePage);
    }

    /**
     * 根据角色ID获取页面资源
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "根据角色获取页面资源", description = "获取指定角色可访问的所有页面资源")
    public ResponseEntity<List<ResourcePageDTO>> getResourcePagesByRoleId(@PathVariable Long roleId) {
        List<ResourcePageDTO> resourcePages = resourcePageService.getResourcePagesByRoleId(roleId);
        return ResponseEntity.ok(resourcePages);
    }

    /**
     * 获取当前用户可访问的页面资源
     */
    @GetMapping("/my-accessible")
    @Operation(summary = "获取我的页面资源", description = "获取当前登录用户可访问的所有页面资源")
    public ResponseEntity<List<ResourcePageDTO>> getMyAccessibleResourcePages(@CurrentUserId Long userId) {
        List<ResourcePageDTO> resourcePages = resourcePageService.getResourcePagesByUserId(userId);
        return ResponseEntity.ok(resourcePages);
    }

    /**
     * 创建页面资源
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建页面资源", description = "创建新的页面资源")
    public ResponseEntity<ResourcePageDTO> createResourcePage(@Valid @RequestBody ResourcePageCreateDTO resourcePageCreateDto) {
        ResourcePageDTO createdResourcePage = resourcePageService.createResourcePage(resourcePageCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResourcePage);
    }

    /**
     * 更新页面资源
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新页面资源", description = "更新指定页面资源的信息")
    public ResponseEntity<ResourcePageDTO> updateResourcePage(@PathVariable Long id, @Valid @RequestBody ResourcePageUpdateDTO resourcePageUpdateDto) {
        ResourcePageDTO updatedResourcePage = resourcePageService.updateResourcePage(id, resourcePageUpdateDto);
        return ResponseEntity.ok(updatedResourcePage);
    }

    /**
     * 删除页面资源
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除页面资源", description = "删除指定的页面资源")
    public ResponseEntity<Void> deleteResourcePage(@PathVariable Long id) {
        resourcePageService.deleteResourcePage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 检查用户页面权限
     */
    @GetMapping("/check-permission/{resourcePageCode}/{permission}")
    @Operation(summary = "检查页面权限", description = "检查当前用户是否有指定页面的权限")
    public ResponseEntity<Boolean> checkPermission(@CurrentUserId Long userId, @PathVariable String resourcePageCode, @PathVariable String permission) {
        boolean hasPermission = resourcePageService.hasPermission(userId, resourcePageCode, permission);
        return ResponseEntity.ok(hasPermission);
    }
}
