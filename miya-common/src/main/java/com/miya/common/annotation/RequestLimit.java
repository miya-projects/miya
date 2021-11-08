package com.miya.common.annotation;

import org.springframework.core.annotation.Order;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 单接口访问次数限制
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
// @Order(Ordered.HIGHEST_PRECEDENCE)
public @interface RequestLimit {
    /**
     * 允许访问的次数
     */
    int count();

    /**
     *
     * 时间段，单位为秒，默认值一分钟
     */
    int seconds() default 60;
}
