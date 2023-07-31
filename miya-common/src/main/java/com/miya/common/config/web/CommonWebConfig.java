package com.miya.common.config.web;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miya.common.config.web.converter.StringToTimeLikeConverter;
import com.miya.common.config.web.interceptor.ActionLogInterceptor;
import com.miya.common.config.web.interceptor.ApiRequestLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class CommonWebConfig implements WebMvcConfigurer {

    /**
     * 添加json参数解析器
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestJsonHandlerMethodArgumentResolver(SpringUtil.getBean(ConversionService.class)));
        argumentResolvers.add(new AuthenticationPrincipalHandlerMethodArgumentResolver());
    }

    /**
     * 增加日期类型转换器
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToTimeLikeConverter());
    }


    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加日志拦截器
        registry.addInterceptor(SpringUtil.getBean(ActionLogInterceptor.class)).addPathPatterns(Collections.singletonList("/api/**"));
        //api访问次数限制拦截器，限制ip
        // registry.addInterceptor(apiUsageLimitInterceptor()).addPathPatterns(Collections.singletonList("/api/**"));
        //单个api访问次数限制拦截器，限制用户
        registry.addInterceptor(new ApiRequestLimitInterceptor()).addPathPatterns(Collections.singletonList("/api/**"));
    }

    // @Bean
    // @ConditionalOnMissingBean(ApiUsageLimitInterceptor.class)
    // public ApiUsageLimitInterceptor apiUsageLimitInterceptor(){
    //     return new ApiUsageLimitInterceptor();
    // }

    @Bean
    @ConditionalOnMissingBean
    public ActionLogInterceptor actionLogInterceptor(ObjectMapper objectMapper){
        return new ActionLogInterceptor(objectMapper, false, true, new String[]{"com.miya.system.module.common.MonitorAndMaintenanceApi"});
    }

}
