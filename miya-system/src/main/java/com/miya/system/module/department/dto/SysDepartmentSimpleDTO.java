package com.miya.system.module.department.dto;

import com.miya.common.config.xlsx.ToExcelFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class SysDepartmentSimpleDTO implements ToExcelFormat {

    private String id;

    /**
     * 部门名称
     */
    private String name;

    @Override
    public String toStringForExcel() {
        return name;
    }
}
