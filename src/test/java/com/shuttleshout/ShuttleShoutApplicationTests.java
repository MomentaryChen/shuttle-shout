package com.shuttleshout;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.shuttleshout.common.util.PasswordUtil;

/**
 * 应用测试类
 * 
 * @author ShuttleShout Team
 */
@SpringBootTest
class ShuttleShoutApplicationTests {

    private final PasswordUtil passwordUtil = new PasswordUtil();

    @Test
    void contextLoads() {
        // 测试Spring上下文加载
    }

   
}
