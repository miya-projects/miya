package com.miya.system.module.user.model;

import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.dto.SysDepartmentSimpleDTO;
import com.miya.system.module.oss.model.SysFileDTO;
import com.miya.system.module.role.model.SysRoleSimpleDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserListDTO extends BaseDTO {

    private String username;

    private String name;

    private String remark;

    private String phone;

    private SysFileDTO avatar;

    private SysUser.Sex sex;

    private Set<SysRoleSimpleDTO> roles;

    private SysUser.AccountStatus accountStatus;

    private Set<SysDepartmentSimpleDTO> departments;

    private boolean isSuperAdmin;

    public static SysUserListDTO of(SysUser user) {
        return modelMapper.map(user, SysUserListDTO.class);
    }

}
