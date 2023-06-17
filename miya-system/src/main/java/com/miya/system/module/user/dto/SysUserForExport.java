package com.miya.system.module.user.dto;

import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.dto.SysDepartmentSimpleDTO;
import com.miya.system.module.role.model.SysRoleSimpleDTO;
import com.miya.system.module.user.model.SysUser;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SysUserForExport extends BaseDTO {

    private String username;

    private String name;

    private String phone;

    private SysUser.Sex sex;

    private Set<SysRoleSimpleDTO> roles;

    private SysUser.AccountStatus accountStatus;

    private Set<SysDepartmentSimpleDTO> departments;

    private String remark;

    public static SysUserForExport of(SysUser user) {
        return modelMapper.map(user, SysUserForExport.class);
    }

}
