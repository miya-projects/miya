package com.miya.system.module.user.model;

import com.miya.common.annotation.FieldMapping;
import com.miya.common.module.base.BaseForm;
import com.miya.system.module.oss.model.SysFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

/**
 * 用于自己修改个人信息
 */
@ApiModel
@Getter
@Setter
public class SysUserModifyForm extends BaseForm<SysUser> {

    @ApiModelProperty("姓名")
    @NotBlank
    private String name;

    @ApiModelProperty("头像文件id")
    @FieldMapping(mappingClass = SysFile.class)
    private String avatar;

    @ApiModelProperty("性别")
    private SysUser.Sex sex;

}
