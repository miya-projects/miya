package com.miya.common.module.bod;

import java.lang.annotation.*;

/**
 * 标记该注解的entity在删除时会备份到另外的数据库中
 * 该注解可继承
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface BackupOnDelete {
}
