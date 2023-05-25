package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class SysDictItemForm extends BaseForm<SysDictItem> {

    @NotBlank(message = "字典值不能为空")
    private String value;

    private String label;

}
