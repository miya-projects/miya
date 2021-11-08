package com.miya.common.auth.way;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 保存用户登录信息
 */
@Getter
@Setter
@Builder
public class LoginInfo {
    /**
     * 登录设备
     */
    private LoginDevice loginDevice;
    /**
     * 登录方式
     */
    private LoginWay loginWay;
    /**
     * 登录时间
     */
    private Date loginTime;
}
