package com.miya.system.module.role.model;

import com.miya.common.module.base.BaseDTO;
import com.miya.system.config.business.Business;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Schema
@Getter
@Setter
public class SysRoleDTO extends BaseDTO {

    @NotBlank
    @Schema(description = "角色名")
    private String name;
    @Schema(description = "顺序")
    private Integer sequence;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "是否是系统角色")
    private Boolean isSystem;

    @Schema(description = "该角色拥有的权限")
    private Set<Business> business;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysRoleDTO that = (SysRoleDTO) o;
        return name.equals(that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    public static SysRoleDTO of(SysRole sysRole) {
        return modelMapper.map(sysRole, SysRoleDTO.class);
    }

}
