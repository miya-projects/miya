package com.miya.system.config.web;

import com.miya.common.config.web.interceptor.SignAccessInterceptor;
import com.miya.common.module.specificationbinds.QueryBindingConfigurator;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.filter.interceptors.ApiAccessInterceptor;
import com.miya.common.module.specificationbinds.DefaultQueryBindingConfigurator;
import com.miya.common.module.specificationbinds.SpecificationArgumentResolver;
import com.miya.system.module.FlagForMiyaSystemModule;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * @author 杨超辉
 */
@Slf4j
@Configuration
public class MiyaSystemWebConfig implements WebMvcConfigurer {

    @Resource
    private ProjectConfiguration projectConfiguration;
    @Resource
    private ApiAccessInterceptor apiAccessInterceptor;
    @Resource
    private QueryBindingConfigurator queryBindingConfigurator;

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new SpecificationArgumentResolver(queryBindingConfigurator));
    }
}
