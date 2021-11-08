package com.teamytd.config.swagger;

import com.miya.system.config.swagger.DocketBuilder;
import com.miya.system.config.swagger.SwaggerConfiguration;
import com.teamytd.Application;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public DocketBuilder video(){
        return new DocketBuilder("视频管理",
                SwaggerConfiguration.ApiSelectors.packageApiSelector(Application.class.getPackage().getName()));
    }
}
