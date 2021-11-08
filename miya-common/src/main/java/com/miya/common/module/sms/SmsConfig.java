package com.miya.common.module.sms;

import com.miya.common.module.sms.service.impl.LogSmsService;
import com.miya.common.module.cache.KeyValueStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// @EnableConfigurationProperties(SmsConfigProperties.class)
public class SmsConfig {

    @Bean
    // @ConditionalOnProperty(prefix = "config.sms", name = "type", havingValue = "minio")
    @ConditionalOnMissingBean
    public LogSmsService logSmsService(KeyValueStore keyValueStore){
        return new LogSmsService(keyValueStore);
    }



}
