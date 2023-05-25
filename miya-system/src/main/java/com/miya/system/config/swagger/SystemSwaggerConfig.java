package com.miya.system.config.swagger;

import com.miya.system.module.FlagForMiyaSystemModule;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemSwaggerConfig {

    @Bean
    public GroupedOpenApi systemOpenApi() {
        return GroupedOpenApi.builder().group("系统")
//                .addOperationCustomizer()
                .packagesToScan(FlagForMiyaSystemModule.class.getPackageName())
                .build();
    }

}
