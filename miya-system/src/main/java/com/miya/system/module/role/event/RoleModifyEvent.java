package com.miya.system.module.role.event;

import com.miya.system.module.log.event.LogEvent;
import com.miya.system.module.role.model.SysRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 角色修改相关事件
 */
public class RoleModifyEvent extends LogEvent {

    @RequiredArgsConstructor
    public enum RoleModifyType {
        MODIFY_PERMISSION("修改角色权限"),
        MODIFY("修改角色信息"),
        NEW("新增角色"),
        DELETE("删除角色"),
        ;
        @Getter
        private final String name;
    }

    public RoleModifyEvent(SysRole role, RoleModifyType roleModifyType) {
        super(roleModifyType.getName(), roleModifyType.getName(), role.getId());
    }

}
