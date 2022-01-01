package com.miya.system.config.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.Collections;

/**
 * @author 杨超辉
 * servlet filter注册
 */
@Configuration
@Slf4j
public class FilterConfig {

    private <T extends Filter> FilterRegistrationBean<T> getFilterRegistrationBean(int order, String urls, T filter) {
        FilterRegistrationBean<T> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setOrder(order);
        filterRegistrationBean.setUrlPatterns(Collections.singleton(urls));
        filterRegistrationBean.setFilter(filter);
        return filterRegistrationBean;
    }

//    @Bean
//    public FilterRegistrationBean apiAccessFilter(){
//        FilterRegistrationBean filterRegistrationBean = getFilterRegistrationBean(1, "/**", new ApiAccessFilter());
//        return filterRegistrationBean;
//    }

}
