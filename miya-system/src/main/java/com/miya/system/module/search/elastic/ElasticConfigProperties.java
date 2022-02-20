package com.miya.system.module.search.elastic;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "config.es")
public class ElasticConfigProperties {

    /**
     * ES连接域名
     */
    private String hosts;

    /**
     * 协议
     */
    private String protocol = "http";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
