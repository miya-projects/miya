package com.miya.common.module.sms.service;


/**
 * 手机短信服务
 */
public interface SmsService {

    /**
     * 给指定手机号发送短信
     * @param phone
     * @param content
     */
    void sendSms(String phone, String content);

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @param verifyCode    验证码
     */
    void sendVerifyCode(String phone, String verifyCode);

}
