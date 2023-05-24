package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseForm;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@ApiModel
@Getter
@Setter
public class SysDictForm extends BaseForm<SysDict> {

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String name;
    /**
     * 业务代码
     */
    @NotBlank(message = "字典code不能为空")
    private String code;

}
