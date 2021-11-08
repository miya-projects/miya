package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseForm;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@ApiModel
@Getter
@Setter
public class SysDictItemForm extends BaseForm<SysDictItem> {

    @NotBlank(message = "字典值不能为空")
    private String value;

    private String label;

}
