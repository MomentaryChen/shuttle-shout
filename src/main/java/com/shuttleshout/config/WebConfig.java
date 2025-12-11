package com.shuttleshout.config;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * Web配置类
 * 
 * @author ShuttleShout Team
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    // CORS配置已移至SecurityConfig中統一管理，避免配置衝突
    // 如果需要在非Security過濾的端點使用CORS，可以取消下面的註釋
    /*
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type", "X-Total-Count")
                .allowCredentials(false)
                .maxAge(3600);
    }
    */

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }
}
