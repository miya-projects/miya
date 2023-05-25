package com.miya.system.module.role.model;

import cn.hutool.core.util.StrUtil;
import com.miya.common.module.base.BaseForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class SysRoleForm extends BaseForm<SysRole> {

    @NotBlank
    @Schema(description = "角色名")
    private String name;
    @Schema(description = "顺序")
    private Integer sequence;
    @Schema(description = "描述")
    private String description;

    public void setName(String name) {
        this.name = StrUtil.trim(name);
    }
}
