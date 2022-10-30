package com.miya.system.module.user.model;

import com.miya.common.service.mapper.DTOMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserDetailDTO extends SysUserListDTO {

    @ApiModelProperty("权限代码")
    private Set<String> business;

    @ApiModelProperty("偏好配置")
    private SysUser.Preferences preferences;


    public static SysUserDetailDTO of(SysUser user) {
        return Mapper.INSTANCE.toDto(user);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysUserDetailDTO, SysUser> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);

        @Mapping(target = "avatar", expression = "java(entity.getAvatar().getUrl())")
        @Override
        SysUserDetailDTO toDto(SysUser entity);
    }
}
