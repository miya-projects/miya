package com.miya.system.module.user.event;

import com.miya.system.module.log.event.LogEvent;
import com.miya.system.module.user.model.SysUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 修改用户事件
 */
public class UserModifyEvent extends LogEvent {

    @RequiredArgsConstructor
    public enum UserModifyType{
        RESET_PASSWORD("重置用户密码"),
        MODIFY_USERINFO("修改用户信息"),
        FREEZE("冻结用户"),
        UNFREEZE("解结用户"),
        NEW("新增用户"),
        DELETE("删除用户"),
        ;
        @Getter
        private final String name;
    }

    public UserModifyEvent(SysUser sysUser, UserModifyType userModifyType) {
        super(userModifyType.getName(), userModifyType.getName(), sysUser.getId());
    }
}
