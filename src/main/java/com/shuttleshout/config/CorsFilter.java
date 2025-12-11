package com.shuttleshout.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 自定義CORS過濾器
 * 注意：CORS配置已移至SecurityConfig中統一管理，此過濾器僅處理特殊情況
 *
 * @author ShuttleShout Team
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 注意：主要的CORS配置已在SecurityConfig中處理
        // 此處僅確保OPTIONS預檢請求正確響應，避免Spring Security的CORS處理中可能的問題

        // 對於所有請求，添加CORS頭部（作為Spring Security的補充）
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            // 檢查Origin是否為允許的來源
            if (isAllowedOrigin(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
                response.setHeader("Access-Control-Allow-Headers",
                    "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-AUTHORIZATION, " +
                    "Access-Control-Request-Method, Access-Control-Request-Headers");
                response.setHeader("Access-Control-Max-Age", "3600");

                // 暴露必要的頭部
                response.setHeader("Access-Control-Expose-Headers",
                    "Authorization, X-AUTHORIZATION, Content-Type, X-Total-Count");
            }
        }

        // 對於OPTIONS預檢請求，直接響應
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 繼續過濾鏈，讓Spring Security的CORS配置處理其他請求
        chain.doFilter(req, res);
    }

    /**
     * 檢查Origin是否為允許的來源
     */
    private boolean isAllowedOrigin(String origin) {
        // 開發環境允許的來源
        return origin.startsWith("http://localhost:") ||
               origin.startsWith("http://127.0.0.1:") ||
               origin.equals("http://localhost:3000") ||
               origin.equals("http://localhost:3001") ||
               origin.equals("http://127.0.0.1:3000") ||
               origin.equals("http://127.0.0.1:3001");
    }
}

