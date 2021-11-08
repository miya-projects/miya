package com.miya.system.module.user;

import com.miya.common.service.JwtTokenService;
import com.miya.system.config.ProjectConfiguration;
import com.miya.common.config.web.jwt.TokenService;
import com.miya.common.module.cache.KeyValueStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SysUserConfig {

    @Bean
    @ConditionalOnMissingBean
    public SysUserCustomizer sysUserCustomizer() {
        return SysUserCustomizer.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SysUserService userService(SysUserRepository sysUserRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                                      TokenService tokenService, KeyValueStore keyValueStore,
                                      ProjectConfiguration projectConfiguration, ApplicationContext applicationContext,
                                      SysUserCustomizer customizer, JwtTokenService jwtTokenService) {
        return new SysUserService(sysUserRepository, bCryptPasswordEncoder, tokenService, keyValueStore,
                projectConfiguration, jwtTokenService, applicationContext, customizer);
    }
}
