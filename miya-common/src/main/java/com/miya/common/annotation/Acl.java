package com.miya.common.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识可访问接口的条件,作用在controller上时，对该controller所有方法起作用，
 * 当controller和method同时配置该注解时，method的配置覆盖controller
 * 不配置表示不限制
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Acl {

    /**
     * 可访问的用户类型 该参数也用作swagger文档分组
     */
    @AliasFor("value")
    Class<?> userType() default AllUser.class;

    @AliasFor("userType")
    Class<?> value() default AllUser.class;

    /**
     * 即用到此api的业务功能集合，系统将会为有其中一种功能的角色提供接口服务
     */
    String[] business() default {};

    /**
     * 无需登录即可访问，即允许匿名
     */
    class NotNeedLogin {}

    /**
     * 所有已登录用户都可以访问
     */
    class AllUser {}
}
