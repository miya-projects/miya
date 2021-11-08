package com.teamytd.config.web;

import com.teamytd.module.FlagForModule;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

}
