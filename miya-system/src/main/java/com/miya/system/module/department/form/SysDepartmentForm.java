package com.miya.system.module.department.form;

import com.miya.common.annotation.FieldMapping;
import com.miya.common.module.base.BaseForm;
import com.miya.system.module.department.SysDepartment;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@ApiModel
@Getter
@Setter
public class SysDepartmentForm extends BaseForm<SysDepartment> {
    @NotBlank
    private String name;
    private String description;
    @FieldMapping(mappingClass = SysDepartment.class)
    private String parent;
    private Map<String, Object> extra;

}
