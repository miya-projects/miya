package com.teamytd.config.web;

import com.miya.system.module.oss.MiyaSystemOssConfig;
import com.teamytd.module.FlagForModule;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.ArrayList;
import java.util.Arrays;

@Configuration
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
public class WebConfig implements WebMvcConfigurer {

    /**
     * 路由匹配规则
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/video", HandlerTypePredicate.forBasePackage(FlagForModule.class.getPackage().getName()));
    }

    /**
     * miyaSystemOss配置
     */
    @Bean
    public MiyaSystemOssConfig defaultConfig(){
        ArrayList<String> strings = new ArrayList<>();
        strings.addAll(Arrays.asList(MiyaSystemOssConfig.DEFAULT_ALLOW_SUFFIX));
        strings.add("exe");
        String[] result = strings.toArray(new String[0]);
        return new MiyaSystemOssConfig(){
            @Override
            public String[] allowUploadSuffix() {
                return result;
            }
        };
    }

}
