package com.miya.system.module.department.dto;

import com.miya.common.config.xlsx.ToExcelFormat;
import com.miya.common.module.base.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class SysDepartmentSimpleDTO extends BaseDTO implements ToExcelFormat {
    private String name;

    @Override
    public String toStringForExcel() {
        return name;
    }
}
