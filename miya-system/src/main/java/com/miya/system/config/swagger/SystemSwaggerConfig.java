package com.miya.system.config.swagger;

import com.miya.system.module.FlagForMiyaSystemModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemSwaggerConfig {

    @Bean
    public DocketBuilder back(){
        return new DocketBuilder("系统", SwaggerConfiguration.ApiSelectors.packageApiSelector(FlagForMiyaSystemModule.class.getPackage().getName()));
    }

}
