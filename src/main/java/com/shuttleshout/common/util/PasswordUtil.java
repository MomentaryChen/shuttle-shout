package com.shuttleshout.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 * 
 * @author ShuttleShout Team
 */
@Component
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

