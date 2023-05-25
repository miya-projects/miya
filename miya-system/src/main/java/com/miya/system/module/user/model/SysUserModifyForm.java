package com.miya.system.module.user.model;

import com.miya.common.module.base.BaseForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 用于自己修改个人信息
 */
@Schema
@Getter
@Setter
public class SysUserModifyForm extends BaseForm<SysUser> {

    @Schema(description = "姓名")
    @NotBlank
    private String name;

    @Schema(description = "头像文件id")
    private String avatar;

    @Schema(description = "性别")
    private SysUser.Sex sex;

}
