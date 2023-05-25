package com.miya.system.module.role;

import cn.hutool.extra.spring.SpringUtil;
import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.user.model.SysUser;

import java.util.Set;

/**
 * 需要作为系统内置角色，具有不可动态更改，不可删除的特质
 * 用于制作默认角色枚举需要实现继承的接口
 * {@link SysDefaultRoles }
 */
public interface DefaultRole {

    /**
     * 角色数据库id
     */
    String getId();

    /**
     * 角色名，相当于备注，用于开发人员识别
     */
    String getName();

    /**
     * 获得角色对象
     */
    default SysRole getSysRole(){
        SysRoleService service = SpringUtil.getBean(SysRoleService.class);
        return service.getDefaultRoleById(getId());
    }

    /**
     * 判断该用户是否有该角色
     * @param sysUser
     */
    default boolean hasThisRole(SysUser sysUser) {
        return hasThisRole(sysUser.getRoles());
    }

    /**
     * 判断该角色列表是否有该角色
     * @param sysRole
     */
    default boolean hasThisRole(Set<SysRole> sysRole) {
        return sysRole.stream().map(SysRole::getId).anyMatch(this.getId()::equals);
    }
}
