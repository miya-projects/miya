package com.miya.system.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 项目所有配置
 */
@Getter
@Configuration
@EnableConfigurationProperties(ProjectConfig.class)
@RequiredArgsConstructor
@Component
public class ProjectConfiguration {

    @Delegate
    private final ProjectConfig projectConfig;

    @Value("${server.port}")
    private int port;

    /**
     * 运行环境
     */
    @Value("${spring.profiles.active}")
    private String profile;

    public boolean isProduction(){
        return "prod".contains(this.profile);
    }

    /**
     * 是否开启重放攻击防护
     */
    @Value("false")
    private boolean needSign;
}
