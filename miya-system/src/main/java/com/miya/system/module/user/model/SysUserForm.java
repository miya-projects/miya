package com.miya.system.module.user.model;

import com.miya.common.annotation.constraint.ValidFieldString;
import com.miya.common.module.base.BaseForm;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserForm extends BaseForm<SysUser> {

    @NotBlank
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    @NotNull
    private String name;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("电话")
    @ValidFieldString(type = ValidFieldString.ValidType.PHONE)
    private String phone;

    @ApiModelProperty("头像文件id")
    private String avatar;

    @ApiModelProperty("性别")
    private SysUser.Sex sex;

    private Set<String> roles;

    private Set<String> departments;

}
