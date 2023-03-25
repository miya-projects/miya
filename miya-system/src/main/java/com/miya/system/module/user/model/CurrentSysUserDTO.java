package com.miya.system.module.user.model;

import com.miya.common.module.config.SysConfigService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentSysUserDTO {

    private SysUserDetailDTO user;

    private SysConfigService.SystemMeta systemMeta;
    private Long unreadNoticeAmount;

}
