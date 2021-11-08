package com.miya.system.module.role.model;

import cn.hutool.core.util.StrUtil;
import com.miya.common.module.base.BaseForm;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@ApiModel
@Getter
@Setter
public class SysRoleForm extends BaseForm<SysRole> {

    @NotBlank
    @ApiModelProperty("角色名")
    private String name;
    @ApiModelProperty("顺序")
    private Integer sequence;
    @ApiModelProperty("描述")
    private String description;

    public void setName(String name) {
        this.name = StrUtil.trim(name);
    }
}
