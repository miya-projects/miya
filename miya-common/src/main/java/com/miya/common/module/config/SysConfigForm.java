package com.miya.common.module.config;

import com.miya.common.module.base.BaseForm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

/**
 * 系统参数
 */
@Getter
@Setter
@Schema
public class SysConfigForm extends BaseForm<SysConfig> {
    //变量key
    @Schema(description = "参数key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数key不能为空")
    private String key;
    //变量值
    @Schema(description = "变量值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "默认值不能为空")
    private String val;
    //备注
    @Schema(description = "备注")
    private String desc;

}
