package com.miya.third.sms.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.miya.common.util.JSONUtils;
import com.miya.third.sms.exception.NotSupportedException;
import com.miya.third.sms.service.SmsService;
import lombok.SneakyThrows;
import java.util.List;
import java.util.Map;

/**
 * 阿里云短信模块
 */
public class AliyunSmsService implements SmsService {

    private final com.aliyun.dysmsapi20170525.Client client;

    public AliyunSmsService(String accessKeyId, String accessKeySecret) {
        client = createClient(accessKeyId, accessKeySecret);
    }

    @Override
    public void sendSms(String phone, String content) {
        throw new NotSupportedException();
    }

    /**
     * 使用ID和Secret初始化账号Client
     * @return Client
     */
    @SneakyThrows
    public static Client createClient(String accessKeyId, String accessKeySecret) {
        Config config = new Config()
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
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setTemplateParam("{\"code\": \"" + verifyCode + "\"}");
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            client.sendSmsWithOptions(sendSmsRequest, runtime);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            Common.assertAsString(error.message);
        }
    }

    /**
     * 发送短信
     * @param phone 手机号
     * @param signName  签名
     * @param templateCode 模板代码
     * @param params 模板参数
     */
    public SendSmsResponse sendSms(String phone, String signName, String templateCode, Map<String, Object> params) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(JSONUtils.toJson(params));
        try {
            return client.sendSms(sendSmsRequest);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            Common.assertAsString(error.message);
            throw new RuntimeException(_error);
        }
    }

    /**
     * 批量发送短信
     * @param phones 手机号
     * @param signNames  签名
     * @param templateCode 模板代码
     * @param params 模板参数
     */
    public SendBatchSmsResponse sendBatchSms(List<String> phones, List<String> signNames, String templateCode, List<Map<String, Object>> params) {
        SendBatchSmsRequest sendSmsRequest = new SendBatchSmsRequest()
                .setPhoneNumberJson(JSONUtils.toJson(phones))
                .setSignNameJson(JSONUtils.toJson(signNames))
                .setTemplateCode(templateCode)
                .setTemplateParamJson(JSONUtils.toJson(params));
        try {
            return client.sendBatchSms(sendSmsRequest);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            Common.assertAsString(error.message);
            throw new RuntimeException(_error);
        }
    }

    /**
     * 查询短信模板列表
     * @param pageIndex 默认为1
     * @param pageSize 1-50，默认为10
     */
    public QuerySmsTemplateListResponse querySmsTemplate(Integer pageIndex, Integer pageSize) {
        QuerySmsTemplateListRequest querySmsTemplateRequest = new QuerySmsTemplateListRequest();
        querySmsTemplateRequest.setPageIndex(pageIndex);
        querySmsTemplateRequest.setPageSize(pageSize);
        QuerySmsTemplateListResponse response = null;
        try {
            response = client.querySmsTemplateList(querySmsTemplateRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    /**
     * 查询签名列表
     * @param pageIndex 默认为1
     * @param pageSize 1-50，默认为10
     */
    public QuerySmsSignListResponse querySignList(Integer pageIndex, Integer pageSize) {
        QuerySmsSignListRequest querySmsSignListRequest = new QuerySmsSignListRequest();
        querySmsSignListRequest.setPageIndex(pageIndex);
        querySmsSignListRequest.setPageSize(pageSize);
        RuntimeOptions runtime = new RuntimeOptions();
        QuerySmsSignListResponse querySmsSignListResponse = null;
        try {
            querySmsSignListResponse = client.querySmsSignListWithOptions(querySmsSignListRequest, runtime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return querySmsSignListResponse;
    }

    /**
     * 校验签名是否可用
     * @param signName 待校验的签名
     */
    public boolean validateSignName(String signName) {
        QuerySmsSignListResponse response = querySignList(1, 50);
        return response.getBody().getSmsSignList().stream()
                .anyMatch(sign -> sign.getSignName().equals(signName) && sign.getAuditStatus().equals("AUDIT_STATE_PASS"));
    }

    /**
     * 校验模板代码是否可用
     * @param templateCode 待校验的模板代码
     */
    public boolean validateTemplateCode(String templateCode) {
        QuerySmsTemplateListResponse response = querySmsTemplate(1, 50);
        return response.getBody().getSmsTemplateList().stream()
                .anyMatch(template -> template.getTemplateCode().equals(templateCode) && template.getAuditStatus().equals("AUDIT_STATE_PASS"));
    }

    public static void main(String[] args) {
        AliyunSmsService aliyunSmsService = new AliyunSmsService("", "");
        //QuerySmsTemplateListResponse response = aliyunSmsService.querySmsTemplate(1, 50);
        //QuerySmsSignListResponse querySmsSignListResponse = aliyunSmsService.querySignList(1, 50);
        System.out.println("end");
    }
}
