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

    @Schema(description = "配置分组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置分组不能为空")
    private String group;


    @Schema(description = "参数key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "参数key不能为空")
    private String key;

    @Schema(description = "变量值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "默认值不能为空")
    private String val;

    @Schema(description = "备注")
    private String desc;

}
