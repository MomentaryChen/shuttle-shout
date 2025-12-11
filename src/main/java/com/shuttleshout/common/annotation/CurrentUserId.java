package com.shuttleshout.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前用户ID注解
 * 用于在Controller方法参数中自动注入当前登录用户的ID
 * 
 * @author ShuttleShout Team
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}

