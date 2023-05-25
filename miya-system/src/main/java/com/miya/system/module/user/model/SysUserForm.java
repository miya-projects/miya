package com.miya.system.module.user.model;

import com.miya.common.annotation.constraint.ValidFieldString;
import com.miya.common.module.base.BaseForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Schema
@Getter
@Setter
public class SysUserForm extends BaseForm<SysUser> {

    @NotBlank
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "姓名")
    @NotNull
    private String name;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "电话")
    @ValidFieldString(type = ValidFieldString.ValidType.PHONE)
    private String phone;

    @Schema(description = "头像文件id")
    private String avatar;

    @Schema(description = "性别")
    private SysUser.Sex sex;

    private Set<String> roles;

    private Set<String> departments;

}
