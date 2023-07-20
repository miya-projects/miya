package com.miya.common.module.sms.service.impl;

import com.aliyun.tea.TeaException;
import com.miya.common.module.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * TODO
 */
@RequiredArgsConstructor
public class AliyunSmsService implements SmsService {

    private final String accessKeyId;
    private final String accessKeySecret;


    @Override
    public void sendSms(String phone, String content) {

    }

    /**
     * 使用ID和Secret初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     */
    @SneakyThrows
    public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

    @Override
    public void sendVerifyCode(String phone, String verifyCode) {
        com.aliyun.dysmsapi20170525.Client client = createClient(accessKeyId, accessKeySecret);
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setTemplateParam("{\"code\": \"" + verifyCode + "\"}");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.sendSmsWithOptions(sendSmsRequest, runtime);
        } catch (TeaException error) {
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
        }
    }

    public static void main(String[] args) {
        new AliyunSmsService("", "").sendVerifyCode("138000000", "985211");
    }
}
