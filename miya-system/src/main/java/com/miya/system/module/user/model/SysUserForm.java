package com.miya.system.module.user.model;

import com.miya.common.annotation.FieldMapping;
import com.miya.common.annotation.constraint.ValidFieldString;
import com.miya.common.module.base.BaseForm;
import com.miya.system.module.department.SysDepartment;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.role.model.SysRole;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserForm extends BaseForm<SysUser> {

    @NotBlank
    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("电话")
    @ValidFieldString(type = ValidFieldString.ValidType.PHONE)
    private String phone;

    @ApiModelProperty("头像文件id")
    @FieldMapping(mappingClass = SysFile.class)
    private String avatar;

    @ApiModelProperty("性别")
    private SysUser.Sex sex;

    @FieldMapping(mappingClass = SysRole.class)
    private Set<String> roles;

    @FieldMapping(mappingClass = SysDepartment.class)
    private Set<String> departments;

}
