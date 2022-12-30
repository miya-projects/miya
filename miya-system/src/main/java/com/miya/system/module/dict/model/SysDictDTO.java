package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseDTO;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

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
        return modelMapper.map(sysDict, SysDictDTO.class);
    }

}
