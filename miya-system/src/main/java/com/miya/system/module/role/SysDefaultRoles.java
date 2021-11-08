package com.miya.system.module.role;

import lombok.Getter;

/**
 * 用作 系统角色映射的HASHMAP的key
 */
@Getter
public enum SysDefaultRoles implements DefaultRole {
    ADMIN("1", "管理员");
    /**
     * 角色id
     */
    private final String id;
    /**
     * 角色名 只用作人类识别这个枚举
     */
    private final String name;

    SysDefaultRoles(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
