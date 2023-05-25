package com.teamytd.config.swagger;

import com.teamytd.Application;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi exampleOpenApi() {
        return GroupedOpenApi.builder().group("视频管理")
                .packagesToScan(Application.class.getPackageName())
                .build();
    }

}
