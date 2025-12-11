package com.shuttleshout;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 羽毛球叫号系统主应用类
 * 
 * @author ShuttleShout Team
 * @version 0.1.0
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@MapperScan("com.shuttleshout.repository")
public class ShuttleShoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShuttleShoutApplication.class, args);
    }
}
