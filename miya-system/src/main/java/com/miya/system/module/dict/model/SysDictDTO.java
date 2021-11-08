package com.miya.system.module.dict.model;

import com.miya.common.service.mapper.DTOMapper;
import com.miya.common.module.base.BaseDTO;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

@ApiModel
@Getter
@Setter
public class SysDictDTO extends BaseDTO {

    public SysDictDTO(){
    }

    @QueryProjection
    public SysDictDTO(SysDict sysDict){
        this.name = sysDict.getName();
        this.code = sysDict.getCode();
    }

    private String name;
    private String code;


    public static SysDictDTO of(SysDict sysDict) {
        return Mapper.INSTANCE.toDto(sysDict);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysDictDTO, SysDict> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }
}
