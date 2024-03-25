package com.miya.system.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 登录接口返回的dto
 */
@Setter
@Getter
@Builder
@Schema(description = "登录接口返回的dto")
public class LoginDTO {

    @Schema(description = "token")
    private String token;
    /**
     * token失效日期
     */
    @Schema(description = "token失效日期")
    private Date expiredDate;
    //        private SysUserDTO sysUserDTO;
}
