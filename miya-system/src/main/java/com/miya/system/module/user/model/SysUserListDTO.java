package com.miya.system.module.user.model;

import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.dto.SysDepartmentSimpleDTO;
import com.miya.system.module.oss.model.SysFileDTO;
import com.miya.system.module.role.model.SysRoleSimpleDTO;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class SysUserListDTO extends BaseDTO {

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private SysFileDTO avatar;

    /**
     * 性别
     */
    private SysUser.Sex sex;

    /**
     * 角色
     */
    private Set<SysRoleSimpleDTO> roles;

    /**
     * 账号状态
     */
    private SysUser.AccountStatus accountStatus;

    /**
     * 部门
     */
    private Set<SysDepartmentSimpleDTO> departments;

    /**
     * 是否是超级管理员
     */
    private boolean superAdmin;

    public static SysUserListDTO of(SysUser user) {
        return modelMapper.map(user, SysUserListDTO.class);
    }

}
