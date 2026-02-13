package com.shuttleshout.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mybatisflex.core.query.QueryWrapper;
import com.shuttleshout.common.model.po.RolePO;
import com.shuttleshout.common.model.po.UserPO;
import com.shuttleshout.common.model.po.table.UserPOTableDef;
import com.shuttleshout.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 自定义UserDetailsService
 * 
 * @author ShuttleShout Team
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserById(username);
    }

    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        UserPO user;
        try {
            Long id = Long.parseLong(userId);
            // 必須載入 roles 關聯，否則 @PreAuthorize("hasRole('SYSTEM_ADMIN')") 等無法正確授權
            user = userRepository.selectOneWithRelationsById(id);
            if (user == null) {
                user = userRepository.selectOneById(id);
            }
        } catch (NumberFormatException e) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .where(UserPOTableDef.USER_PO.ID.eq(userId));
            user = userRepository.selectOneByQuery(queryWrapper);
        }

        if (user == null) {
            throw new UsernameNotFoundException("用戶不存在: " + userId);
        }

        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new UsernameNotFoundException("用戶已被禁用: " + userId);
        }

        // 获取用户角色权限
        List<GrantedAuthority> authorities = getAuthorities(user);

        // 使用用户ID作为principal，这样可以在token中存储用户ID
        return User.builder()
                .username(String.valueOf(user.getId()))
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }

    /**
     * 获取用户权限列表
     */
    private List<GrantedAuthority> getAuthorities(UserPO user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            authorities = user.getRoles().stream()
                    .map(RolePO::getCode)
                    .filter(code -> code != null && !code.isEmpty())
                    .map(code -> "ROLE_" + code.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        // 如果没有角色，添加默认角色
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}

