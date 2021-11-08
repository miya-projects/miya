package com.miya.system.module.dict.model;

import com.miya.common.service.mapper.DTOMapper;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

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
        return Mapper.INSTANCE.toDto(sysDict);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysDictDetailDTO, SysDict> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }

}
