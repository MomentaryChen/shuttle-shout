package com.shuttleshout.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 工具类
 * 用于获取当前登录用户信息
 *
 * @author ShuttleShout Team
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户的ID
     * 
     * @return 用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // 如果principal是字符串（用户ID）
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    /**
     * 获取当前登录用户的用户名（实际是用户ID的字符串形式）
     * 
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }

    /**
     * 检查当前用户是否已认证
     *
     * @return 如果已认证返回true，否则返回false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 检查当前用户是否具有指定的角色
     *
     * @param roleCode 角色代码（如 "SYSTEM_ADMIN"）
     * @return 如果用户具有该角色返回true，否则返回false
     */
    public static boolean hasRole(String roleCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleCode.toUpperCase()));
    }

    /**
     * 检查当前用户是否具有指定的角色列表中的任意一个角色
     *
     * @param roleCodes 角色代码列表
     * @return 如果用户具有任一角色返回true，否则返回false
     */
    public static boolean hasAnyRole(String... roleCodes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (String roleCode : roleCodes) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleCode.toUpperCase()))) {
                return true;
            }
        }

        return false;
    }
}

