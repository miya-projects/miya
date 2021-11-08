package com.miya.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * dto和model的字段映射
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapping {

    /**
     * model中的名字，如果dto和model中名字相同，不需要配置
     * @return
     */
    String value() default "";

    /**
     * 映射的外键类型
     * 如SysUserDTO中sex字段为String类型，
     * 映射到SysUser中为SysDict类型，dto中的sex作为SysDict的id
     * 这时此字段配置为SysDict.class
     * @return
     */
    Class<?> mappingClass() default Void.class;

}
