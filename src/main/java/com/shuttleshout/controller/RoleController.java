package com.shuttleshout.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.RoleDTO;
import com.shuttleshout.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 角色控制器
 * 
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/roles")
@Tag(name = "角色管理", description = "角色相关的API接口")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色
     */
    @GetMapping
    @Operation(summary = "获取所有角色", description = "返回系统中所有角色的列表")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        try {
            List<RoleDTO> roles = roleService.getAllRoles();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            throw new ApiException("获取角色列表失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_ROLES_ERROR", e);
        }
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取角色", description = "根据角色ID获取角色详细信息")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        try {
            RoleDTO role = roleService.getRoleById(id);
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            throw new ApiException("获取角色失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_ROLE_ERROR", e);
        }
    }

    /**
     * 根据代码获取角色
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据代码获取角色", description = "根据角色代码获取角色详细信息")
    public ResponseEntity<RoleDTO> getRoleByCode(@PathVariable String code) {
        try {
            RoleDTO role = roleService.getRoleByCode(code);
            return ResponseEntity.ok(role);
        } catch (Exception e) {
            throw new ApiException("获取角色失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_ROLE_ERROR", e);
        }
    }

    /**
     * 创建角色
     */
    @PostMapping
    @Operation(summary = "创建角色", description = "创建新的角色")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDto) {
        try {
            RoleDTO createdRole = roleService.createRole(roleDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (Exception e) {
            throw new ApiException("创建角色失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "CREATE_ROLE_ERROR", e);
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新指定角色的信息")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDto) {
        try {
            RoleDTO updatedRole = roleService.updateRole(id, roleDto);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            throw new ApiException("更新角色失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "UPDATE_ROLE_ERROR", e);
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除指定的角色")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("删除角色失败: " + e.getMessage(), HttpStatus.BAD_REQUEST, "DELETE_ROLE_ERROR", e);
        }
    }
}

