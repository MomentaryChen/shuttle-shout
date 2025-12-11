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
import org.springframework.security.access.prepost.PreAuthorize;

import com.shuttleshout.common.annotation.CurrentUserId;
import com.shuttleshout.common.exception.ApiException;
import com.shuttleshout.common.model.dto.UserCreateDTO;
import com.shuttleshout.common.model.dto.UserDTO;
import com.shuttleshout.common.model.dto.UserUpdateDTO;
import com.shuttleshout.common.util.SecurityUtil;
import com.shuttleshout.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 用戶控制器
 * 
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用戶管理", description = "用戶相關的API接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 獲取所有用戶
     */
    @GetMapping
    @Operation(summary = "獲取所有用戶", description = "返回系統中所有用戶的列表")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new ApiException("獲取用戶列表失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_USERS_ERROR", e);
        }
    }

    /**
     * 根據ID獲取用戶
     */
    @GetMapping("/{id}")
    @Operation(summary = "根據ID獲取用戶", description = "根據用戶ID獲取用戶詳細資訊")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new ApiException("獲取用戶失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_USER_ERROR", e);
        }
    }

    /**
     * 根據用戶名獲取用戶
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根據用戶名獲取用戶", description = "根據用戶名獲取用戶詳細資訊")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            UserDTO user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new ApiException("獲取用戶失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "GET_USER_ERROR", e);
        }
    }

    /**
     * 創建用戶
     */
    @PostMapping
    @Operation(summary = "創建用戶", description = "創建新的用戶")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDto) {
        try {
            UserDTO createdUser = userService.createUser(userCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            throw new ApiException("創建用戶失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "CREATE_USER_ERROR", e);
        }
    }

    /**
     * 更新用戶
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用戶", description = "更新指定用戶的資訊")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDto) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userUpdateDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new ApiException("更新用戶失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "UPDATE_USER_ERROR", e);
        }
    }

    /**
     * 刪除用戶
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除用戶", description = "刪除指定的用戶")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ApiException("刪除用戶失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "DELETE_USER_ERROR", e);
        }
    }

    /**
     * 獲取當前登錄用戶資訊
     * 示例：使用 @CurrentUserId 註解自動注入當前用戶ID
     */
    @GetMapping("/me")
    @Operation(summary = "獲取當前登錄用戶", description = "獲取當前登錄用戶的詳細資訊")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUserId Long userId) {
        try {
            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new ApiException("獲取當前用戶失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_CURRENT_USER_ERROR", e);
        }
    }

    /**
     * 更新當前登錄用戶資訊
     */
    @PutMapping("/me")
    @Operation(summary = "更新當前用戶資訊", description = "更新當前登錄用戶的個人資訊")
    public ResponseEntity<UserDTO> updateCurrentUser(@CurrentUserId Long userId, @Valid @RequestBody UserUpdateDTO userUpdateDto) {
        try {
            UserDTO updatedUser = userService.updateUser(userId, userUpdateDto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            throw new ApiException("更新用戶資訊失敗: " + e.getMessage(), HttpStatus.BAD_REQUEST, "UPDATE_USER_ERROR", e);
        }
    }

    /**
     * 獲取當前登錄用戶資訊（使用工具類方式）
     * 示例：使用 SecurityUtil 工具類獲取當前用戶ID
     */
    @GetMapping("/me/util")
    @Operation(summary = "獲取當前登錄用戶（工具類方式）", description = "使用 SecurityUtil 工具類獲取當前登錄用戶的詳細資訊")
    public ResponseEntity<UserDTO> getCurrentUserByUtil() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            if (userId == null) {
                throw new ApiException("用戶未認證", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
            }
            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("獲取當前用戶失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_CURRENT_USER_ERROR", e);
        }
    }

    /**
     * 人員配置頁面 - 獲取所有用戶（僅限SYSTEM ADMIN）
     * 用於管理員查看和管理所有用戶的人員配置頁面
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "獲取所有用戶（管理員權限）", description = "僅限SYSTEM ADMIN訪問的人員配置頁面，列出所有用戶")
    public ResponseEntity<List<UserDTO>> getAllUsersForAdmin() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new ApiException("獲取用戶列表失敗: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "GET_USERS_ERROR", e);
        }
    }
}

