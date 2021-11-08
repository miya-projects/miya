package com.miya.system.module.department.dto;

import com.miya.common.service.mapper.DTOMapper;
import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.SysDepartment;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

@ApiModel
@Getter
@Setter
public class SysDepartmentSimpleDTO extends BaseDTO {
    private String name;

    public static SysDepartmentSimpleDTO of(SysDepartment department) {
        return Mapper.INSTANCE.toDto(department);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysDepartmentSimpleDTO, SysDepartment> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }
}
