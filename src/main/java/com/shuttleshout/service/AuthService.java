package com.shuttleshout.service;

import javax.validation.Valid;

import com.shuttleshout.common.model.dto.LoginRequest;
import com.shuttleshout.common.model.dto.LoginResponse;
import com.shuttleshout.common.model.dto.RegisterRequest;
import com.shuttleshout.common.model.dto.UserDTO;

/**
 * 認證服務接口
 * 
 * @author ShuttleShout Team
 */
public interface AuthService {

    /**
     * 用戶註冊
     */
    UserDTO register(@Valid RegisterRequest registerRequest);

    /**
     * 用戶登錄
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用戶登出（可選實現）
     */
    void logout(String token);
}

