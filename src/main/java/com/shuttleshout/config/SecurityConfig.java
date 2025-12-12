package com.shuttleshout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import com.shuttleshout.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security配置类
 * 
 * @author ShuttleShout Team
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 明確允許的來源（開發環境使用具體的localhost地址）
        configuration.addAllowedOriginPattern("http://localhost:3000");
        configuration.addAllowedOriginPattern("http://localhost:3001");
        configuration.addAllowedOriginPattern("http://127.0.0.1:3000");
        configuration.addAllowedOriginPattern("http://127.0.0.1:3001");
        // 添加更多可能的开发端口
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("http://127.0.0.1:*");
        // 生產環境可以通過配置屬性動態設置
        // configuration.addAllowedOriginPattern("${cors.allowed-origins:http://localhost:3000}");

        // 允許所有HTTP方法
        configuration.addAllowedMethod("*");

        // 允許所有標頭
        configuration.addAllowedHeader("*");

        // 允許暴露的標頭（讓前端能夠讀取這些響應頭）
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("X-AUTHORIZATION");
        configuration.addExposedHeader("Content-Type");
        configuration.addExposedHeader("X-Total-Count");
        configuration.addExposedHeader("Access-Control-Allow-Origin");
        configuration.addExposedHeader("Access-Control-Allow-Credentials");

        // 允許攜帶憑證（Cookie、Authorization header等）
        configuration.setAllowCredentials(true);

        // 預檢請求快取時間（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（因為使用JWT）
                .csrf().disable()
                // 啟用CORS並使用配置
                .cors().configurationSource(corsConfigurationSource())
                .and()
                // 設置為無狀態會話（使用JWT）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 配置請求授權
                .authorizeRequests()
                // OPTIONS 預檢請求允許所有（必須在第一位）
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 公開訪問的端點
                .antMatchers("/health/**").permitAll()
                .antMatchers("/auth/login", "/auth/register").permitAll()
                .antMatchers(HttpMethod.GET, "/teams").permitAll()
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/error").permitAll()
                // WebSocket端點允許訪問
                .antMatchers("/ws/**").permitAll()
                // 其他請求需要認證
                .anyRequest().authenticated()
                .and()
                // 添加JWT過濾器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

