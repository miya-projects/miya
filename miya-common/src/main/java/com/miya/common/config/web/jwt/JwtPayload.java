package com.miya.common.config.web.jwt;

import com.miya.common.auth.way.LoginDevice;
import com.miya.common.auth.way.LoginWay;
import lombok.*;

import java.util.Date;

/**
 * token payload
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtPayload {
    /**
     * 用户对象类
     */
    private Class<?> userClass;
    /**
     * 用户id
     */
    private String userId;
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
    /**
     * token失效时间
     */
    private Date exp;

}
