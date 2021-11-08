package com.miya.system.module.user.model;

import com.miya.common.service.mapper.DTOMapper;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserDetailDTO extends SysUserListDTO {

    private Set<String> business;


    public static SysUserDetailDTO of(SysUser user) {
        return Mapper.INSTANCE.toDto(user);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysUserDetailDTO, SysUser> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }
}
