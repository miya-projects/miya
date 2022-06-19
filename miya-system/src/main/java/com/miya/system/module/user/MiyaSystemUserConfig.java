package com.miya.system.module.user;

import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.model.SysUserForm;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.Function;

/**
 * miya-system各模块的自定义
 */
@Builder
@Accessors(fluent = true)
@Getter
public class MiyaSystemUserConfig {

    /**
     * 新用户密码生成器
     */
    @Builder.Default
    private Function<SysUserForm, String> passwordGeneratorForNewUser = userForm -> "123456";

    /**
     * 重置密码生成器
     */
    @Builder.Default
    private Function<SysUser, String> passwordGeneratorForReset = user -> "123456";

    /**
     * 定义额外的功能文件路径或文件名
     * {@link com.miya.system.module.role.SysRoleService#DEFAULT_BUSINESS_LOCATION}
     */
    private List<String> businessFileName;

    // todo wapper
    private Function<SysUser, Boolean> onDelete;

}
