package com.miya.system.module.role.model;

import com.miya.common.config.xlsx.ToExcelFormat;
import com.miya.common.module.base.Convertable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class SysRoleSimpleDTO extends Convertable implements ToExcelFormat {

    private String id;

    @Schema(description = "角色名")
    private String name;

    public static SysRoleSimpleDTO of(SysRole sysRole) {
        return modelMapper.map(sysRole, SysRoleSimpleDTO.class);
    }

    @Override
    public String toStringForExcel() {
        return name;
    }
}
