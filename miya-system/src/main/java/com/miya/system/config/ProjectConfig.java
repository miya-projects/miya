package com.miya.system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * miya特有配置
 */
@ConfigurationProperties(prefix = "config")
@Getter
@Setter
public class ProjectConfig {

    /**
     * 发生错误时发送的邮件地址
     */
    private List<String> email;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * 软件版本
     */
    private String version;

}
