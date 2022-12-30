package com.miya.system.module.role.model;

import com.miya.common.module.base.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class SysRoleSimpleDTO extends BaseDTO {

    @ApiModelProperty("角色名")
    private String name;

    public static SysRoleSimpleDTO of(SysRole sysRole) {
        return modelMapper.map(sysRole, SysRoleSimpleDTO.class);
    }

}
