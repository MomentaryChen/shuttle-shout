package com.shuttleshout;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shuttleshout.common.util.PasswordUtil;

public class PasswordUtilTests {

    private final Logger logger = LoggerFactory.getLogger(PasswordUtilTests.class);

    @Test
    void voicTestPassword() {
        String password = "admin123";
        String encodedPassword = new PasswordUtil().encode(password);
        logger.info("encodedPassword: {}", encodedPassword);
    }

}
