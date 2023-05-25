package com.miya.system.module.department.form;

import com.miya.common.module.base.BaseForm;
import com.miya.system.module.department.SysDepartment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Schema
@Getter
@Setter
public class SysDepartmentForm extends BaseForm<SysDepartment> {
    @NotBlank
    private String name;
    private String description;
    private String parent;
    private Map<String, Object> extra;

}
