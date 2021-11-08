package com.miya.common.module.sms.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.miya.common.module.sms.CacheKeys;
import com.miya.common.module.sms.service.SmsService;
import com.miya.common.module.cache.KeyValueStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;


/**
 * 假装发短信，其实打日志
 */
@Transactional
@Slf4j
@RequiredArgsConstructor
public class LogSmsService implements SmsService {

    private final KeyValueStore keyValueStore;

    /**
     * 给指定手机号发送短信
     * @param phone
     * @param content
     */
    public void sendSms(String phone, String content){
        // todo 限流
        log.info("发送短信给{}, 短信内容: {}", phone, content);
    }

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @param verifyCode    验证码
     */
    public void sendVerifyCode(String phone, String verifyCode){
        // todo 限流
        sendSms(phone, "您的验证码为: " + verifyCode);
        Date now = new Date();
        // 5分钟后过期
        DateTime dateTime = DateUtil.offsetMinute(now, 5);
        keyValueStore.set(CacheKeys.PHONE_VERIFY.toCacheKey(phone), verifyCode, dateTime);
    }
}
