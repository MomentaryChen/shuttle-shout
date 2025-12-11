package com.shuttleshout.config;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.shuttleshout.common.annotation.CurrentUserId;

/**
 * 当前用户ID参数解析器
 * 用于在Controller方法中自动注入当前登录用户的ID
 * 
 * @author ShuttleShout Team
 */
@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未认证");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("无法从认证信息中解析用户ID: " + username);
            }
        }
        
        // 如果principal是字符串（用户ID）
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("无法从认证信息中解析用户ID: " + principal);
            }
        }
        
        throw new IllegalStateException("无法获取当前用户ID");
    }
}

