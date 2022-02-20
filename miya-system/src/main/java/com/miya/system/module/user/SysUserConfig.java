package com.miya.system.module.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysUserConfig {

    @Bean
    @ConditionalOnMissingBean
    public SysUserCustomizer sysUserCustomizer() {
        return SysUserCustomizer.builder().build();
    }

}
