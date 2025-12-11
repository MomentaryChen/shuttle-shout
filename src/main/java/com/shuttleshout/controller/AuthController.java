package com.shuttleshout.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shuttleshout.common.model.dto.LoginRequest;
import com.shuttleshout.common.model.dto.LoginResponse;
import com.shuttleshout.common.model.dto.RegisterRequest;
import com.shuttleshout.common.model.dto.UserDTO;
import com.shuttleshout.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 認證控制器
 * 
 * @author ShuttleShout Team
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "認證管理", description = "用戶登錄認證相關的API接口")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Logger log = LoggerFactory.getLogger(AuthController.class);
    /**
     * 用戶註冊
     */
    @PostMapping("/register")
    @Operation(summary = "用戶註冊", description = "註冊新用戶，返回註冊成功的用戶資訊")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Service 層已經拋出 ApiException，直接調用即可
        UserDTO user = authService.register(registerRequest);
        log.info("用戶註冊成功，用戶名: {}, 用戶ID: {}", user.getUsername(), user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * 用戶登錄
     */
    @PostMapping("/login")
    @Operation(summary = "用戶登錄", description = "通過用戶名和密碼登錄，返回JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Service 層已經拋出 ApiException，直接調用即可
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 用戶登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用戶登出", description = "登出當前用戶")
    public ResponseEntity<Void> logout(@RequestBody String token) {
        // Service 層已經拋出 ApiException，直接調用即可
        authService.logout(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

