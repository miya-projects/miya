package com.miya.system.module.dict.model;

import com.miya.common.module.base.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class SysDictItemDTO extends BaseDTO {

    private String value;
    private String label;

    public static SysDictItemDTO of(SysDictItem sysDictItem) {
        return modelMapper.map(sysDictItem, SysDictItemDTO.class);
    }

}
