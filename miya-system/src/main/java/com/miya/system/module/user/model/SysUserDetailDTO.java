package com.miya.system.module.user.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserDetailDTO extends SysUserListDTO {

    @ApiModelProperty("权限代码")
    private Set<String> business;

    @ApiModelProperty("偏好配置")
    private SysUser.Preferences preferences;

    static {
        // TypeMap<SysUser, SysUserDetailDTO> typeMap = modelMapper.typeMap(SysUser.class, SysUserDetailDTO.class);
        // typeMap.addMapping(user -> {
        //             return Optional.ofNullable(user.getAvatar()).map(SysFile::getUrl).orElse(null);
        //         },
        //         SysUserListDTO::setAvatar);
    }

    public static SysUserDetailDTO of(SysUser user) {
        return modelMapper.map(user, SysUserDetailDTO.class);
    }

}
