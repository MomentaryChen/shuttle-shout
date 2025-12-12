package com.shuttleshout.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shuttleshout.common.constants.ApplicationConstants;
import com.shuttleshout.common.util.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * JWT认证过滤器
 * 
 * @author ShuttleShout Team
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        // 跳過 OPTIONS 預檢請求，讓 CORS 處理
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(ApplicationConstants.JWT_TOKEN_HEADER);

        String userId = null;
        String jwt = null;

        if (authHeader != null && !authHeader.trim().isEmpty()) {
            // 支持直接使用token或Bearer token格式
            if (authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            } else {
                jwt = authHeader;
            }
            try {
                userId = jwtUtil.getUsernameFromToken(jwt);
            } catch (Exception e) {
                logger.error("無法從JWT token中獲取用戶名", e);
            }
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserById(userId);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}

