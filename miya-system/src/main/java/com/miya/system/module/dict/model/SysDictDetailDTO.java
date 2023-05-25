package com.miya.system.module.dict.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SysDictDetailDTO extends SysDictDTO {

    public SysDictDetailDTO() {
    }

    @QueryProjection
    public SysDictDetailDTO(SysDict sysDict) {
        super(sysDict);
        this.items = sysDict.getItems().stream().map(SysDictItemDTO::of).collect(Collectors.toList());
    }

    private List<SysDictItemDTO> items;

    public static SysDictDetailDTO of(SysDict sysDict) {
        return modelMapper.map(sysDict, SysDictDetailDTO.class);
    }

}
