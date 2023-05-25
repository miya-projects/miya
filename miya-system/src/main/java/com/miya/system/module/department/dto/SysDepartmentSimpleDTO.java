package com.miya.system.module.department.dto;

import com.miya.common.config.xlsx.ToExcelFormat;
import com.miya.common.module.base.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class SysDepartmentSimpleDTO extends BaseDTO implements ToExcelFormat {
    private String name;

    @Override
    public String toStringForExcel() {
        return name;
    }
}
