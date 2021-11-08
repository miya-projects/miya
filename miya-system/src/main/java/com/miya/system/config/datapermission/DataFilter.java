package com.miya.system.config.datapermission;

import com.miya.system.module.role.SysDefaultRoles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限配置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(DataFilters.class)
public @interface DataFilter {

    /**
     * 过滤器名称
     * @return
     */
    String filter();

    /**
     * 不受数据权限控制的角色列表，如登录用户含有该角色列表中的至少一个角色，则不启用该数据权限过滤
     * @return
     */
    SysDefaultRoles[] noCheckRoles() default {};

    /**
     * 其他额外参数
     * @return
     */
    String[] extraParameters() default {};
}
