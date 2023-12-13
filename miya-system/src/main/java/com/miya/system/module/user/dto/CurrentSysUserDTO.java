package com.miya.system.module.user.dto;

import com.miya.common.module.config.SystemMeta;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentSysUserDTO {

    private SysUserDetailDTO user;

    private SystemMeta systemMeta;
    private Long unreadNoticeAmount;

}
