package com.miya.system.config.web;

import com.miya.common.config.web.interceptor.SignAccessInterceptor;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.filter.interceptors.ApiAccessInterceptor;
import com.miya.system.module.FlagForMiyaSystemModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author 杨超辉
 * SpringSecurity的配置
 * <a href="https://blog.csdn.net/linzhiqiang0316/article/details/78358907">Demo</a>
 */
@Slf4j
@Configuration
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
public class MiyaSystemWebConfig implements WebMvcConfigurer {

    @Resource
    private ProjectConfiguration projectConfiguration;
    @Resource
    private ApiAccessInterceptor apiAccessInterceptor;

    /**
     * 路由匹配规则
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/sys", HandlerTypePredicate.forBasePackage(FlagForMiyaSystemModule.class.getPackage().getName()));
    }

    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //判断当前环境是否是需要签名认证的环境
        if (projectConfiguration.isNeedSign()) {
            //添加签名认证拦截器
            registry.addInterceptor(new SignAccessInterceptor()).addPathPatterns(Collections.singletonList("/api/**"));
        }
        //添加api访问控制拦截器
        registry.addInterceptor(apiAccessInterceptor)
                .addPathPatterns(Collections.singletonList("/api/**"));
    }

}
