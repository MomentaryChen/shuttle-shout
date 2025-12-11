package com.shuttleshout.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP配置类
 * 启用AspectJ自动代理
 * 
 * @author ShuttleShout Team
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AopConfig {
    // AOP配置已通过注解启用
}
