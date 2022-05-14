package com.miya.common.config.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miya.common.annotation.constraint.CustomMessageInterpolator;
import com.miya.common.config.web.interceptor.ActionLogInterceptor;
import com.miya.common.config.web.interceptor.ApiRequestLimitInterceptor;
import com.miya.common.config.web.interceptor.ApiUsageLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableSpringDataWebSupport
public class CommonWebConfig implements WebMvcConfigurer {

    /**
     * 添加json参数解析器
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new RequestJsonHandlerMethodArgumentResolver());
    }

    /**
     * 增加日期类型转换器
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        //这里改为lambda会报错
        Converter<String, Date> converter = new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                return Convert.toDate(source);
            }
        };
        Converter<String, Timestamp> timeStampConverter = new Converter<String, Timestamp>() {
            @Override
            public Timestamp convert(String source) {
                return Optional.ofNullable(Convert.toLocalDateTime(source)).map(Timestamp::valueOf).orElse(null);
            }
        };
        Converter<String, LocalDate> localDateConverter = new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                LocalDateTime localDateTime = Convert.toLocalDateTime(source);
                if (localDateTime == null){
                    return null;
                }
                return LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
            }
        };
        registry.addConverter(converter);
        registry.addConverter(timeStampConverter);
        registry.addConverter(localDateConverter);
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

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setMessageInterpolator(new CustomMessageInterpolator());
        return localValidatorFactoryBean;
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
