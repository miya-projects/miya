package com.miya.system.module.dict.model;

import com.miya.common.service.mapper.DTOMapper;
import com.miya.common.module.base.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

@Getter
@Setter
@ApiModel
public class SysDictItemDTO extends BaseDTO {

    private String value;
    private String label;

    public static SysDictItemDTO of(SysDictItem sysDictItem) {
        return Mapper.INSTANCE.toDto(sysDictItem);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysDictItemDTO, SysDictItem> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }
}
