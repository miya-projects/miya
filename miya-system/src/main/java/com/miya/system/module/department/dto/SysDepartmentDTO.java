package com.miya.system.module.department.dto;

import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.SysDepartment;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@ApiModel
@Getter
@Setter
public class SysDepartmentDTO extends BaseDTO {
    private String name;
    private String description;
    private List<SysDepartmentDTO> children;
    private Map<String, Object> extra;

    public static SysDepartmentDTO of(SysDepartment sysDepartment) {
        return modelMapper.map(sysDepartment, SysDepartmentDTO.class);
    }
}
