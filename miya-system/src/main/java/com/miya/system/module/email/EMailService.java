package com.miya.system.module.email;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.miya.system.config.ProjectConfiguration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 邮件服务
 */
@Slf4j
@Service
@ManagedResource
public class EMailService {

    private final ProjectConfiguration projectConfiguration;
    /**
     * 为空时，不发送邮件
     */
    private final MailAccount account;

    public EMailService(ProjectConfiguration projectConfiguration, @Autowired(required = false) MailAccount account) {
        this.projectConfiguration = projectConfiguration;
        this.account = account;
        if (this.account == null){
            this.sendEmail = false;
        }
    }

    /**
     * 是否真正地发邮件
     */
    @Setter
    private boolean sendEmail = true;

    /**
     * 发送普通文本邮件
     * @param tos 目的邮箱
     * @param subject   主题
     * @param content   内容
     * @param files 附件
     */
    public void sendText(Collection<String> tos, String subject, String content, File... files) {
        if (!sendEmail){
            return;
        }
        MailUtil.send(account, tos, subject, content, false, files);
        tos.forEach(to -> log.info("发送邮件: to: {}, subject: {}, content: {}", to, subject, content));
    }

    /**
     * 发送html格式邮件
     * @param tos   目的邮箱
     * @param subject   主题
     * @param content   内容
     * @param files 附件
     */
    public void sendHtml(Collection<String> tos, String subject, String content, File... files) {
        if (!sendEmail){
            return;
        }
        MailUtil.send(account, tos, subject, content, true, files);
        tos.forEach(to -> log.info("发送邮件: to: {}, subject: {}, content: {}", to, subject, content));
    }


    /**
     * 给发送日志
     * @param logContent    日志内容
     */
    public void sendLog(String logContent) {
        List<String> emails = projectConfiguration.getEmail();
        if (CollUtil.isEmpty(emails)) {
            return;
        }
        sendText(emails, projectConfiguration.getApplicationName() + "系统发生异常", logContent);
    }

    /**
     * 发送普通文本邮件
     * @param to        目的邮箱
     * @param subject   主题
     * @param content   内容
     */
    @ManagedOperation
    public void sendText(String to, String subject, String content) {
        if (!sendEmail){
            return;
        }
        MailUtil.send(account, Collections.singleton(to), subject, content, false);
    }

}
