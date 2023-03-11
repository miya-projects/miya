package com.miya.system.module.department.dto;

import com.miya.common.module.base.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class SysDepartmentSimpleDTO extends BaseDTO {
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
