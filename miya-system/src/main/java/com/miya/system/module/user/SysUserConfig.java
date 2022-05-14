package com.miya.system.module.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SysUserConfig {

    @Bean
    @ConditionalOnMissingBean
    public SysUserCustomizer sysUserCustomizer() {
        return SysUserCustomizer.builder().build();
    }

    /**
     * hash密码
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
