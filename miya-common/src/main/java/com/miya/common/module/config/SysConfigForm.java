package com.miya.common.module.config;

import com.miya.common.module.base.BaseForm;
import com.miya.common.module.config.SysConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 系统参数
 */
@Getter
@Setter
@ApiModel
public class SysConfigForm extends BaseForm<SysConfig> {
    //变量key
    @ApiModelProperty(value = "参数key", required = true)
    @NotBlank(message = "参数key不能为空")
    private String skey;
    //变量值
    @ApiModelProperty(value = "变量值", required = true)
    @NotBlank(message = "默认值不能为空")
    private String sval;
    //备注
    @ApiModelProperty("备注")
    private String remark;

}
