package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseDTO;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
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
        return modelMapper.map(sysDict, SysDictDTO.class);
    }

}
