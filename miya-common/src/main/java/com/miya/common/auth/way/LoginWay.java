package com.miya.common.auth.way;

/**
 * 登录方式
 */
public enum LoginWay {

    /**
     * 用户名 + 密码
     */
    USERNAME_AND_PASSWORD,
    PHONE_AND_CODE,
    WXCODE,

    /**
     * 用其他用户登录
     */
    LOGIN_AS,

}
