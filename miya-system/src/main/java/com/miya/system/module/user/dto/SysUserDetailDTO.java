package com.miya.system.module.user.dto;

import com.miya.system.module.user.model.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Schema
@Getter
@Setter
public class SysUserDetailDTO extends SysUserListDTO {

    @Schema(description = "权限代码")
    private Set<String> business;

    @Schema(description = "偏好配置")
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
