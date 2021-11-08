package com.miya.system.module.department.dto;

import com.miya.common.service.mapper.DTOMapper;
import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.SysDepartment;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

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

    public static SysDepartmentDTO of(SysDepartment department) {
        return Mapper.INSTANCE.toDto(department);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysDepartmentDTO, SysDepartment> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }
}
