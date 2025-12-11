package com.shuttleshout.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * MyBatis-Flex配置类
 * 
 * @author ShuttleShout Team
 */
@Configuration
public class MyBatisFlexConfig {

    /**
     * 配置MyBatis-Flex审计功能（可选）
     */
    @PostConstruct
    public void init() {
        // 开启审计功能
        AuditManager.setAuditEnable(true);
        
        // 设置 SQL 审计收集器
        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
    }
}
