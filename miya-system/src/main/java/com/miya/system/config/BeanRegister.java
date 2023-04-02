package com.miya.system.config;

import cn.hutool.extra.spring.SpringUtil;
import com.miya.system.listener.event.SystemStartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

/**
 * 注册bean都在这
 */
@Lazy(false)
@Configuration
@Slf4j
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class BeanRegister implements ApplicationContextAware {


    /**
     * 应用启动监听器
     */
    @Bean
    public ApplicationRunner builds(ApplicationContext applicationContext){
        return (args) -> new Thread(() -> applicationContext.publishEvent(new SystemStartupEvent())).start();
    }

    /**
     * 分页从1开始?
     */
    // @Bean
    // public PageableHandlerMethodArgumentResolverCustomizer pageableResolverCustomizer() {
    //     return pageableResolver -> pageableResolver.setOneIndexedParameters(true);
    // }


//    /**
//     * spring缓存配置，使用guava
//     * @return
//     */
//    @Bean
//    public CacheManager cacheManager(){
//        GuavaCacheManager cacheManager = new GuavaCacheManager();
//        cacheManager.setCacheBuilder(CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS));
//        return cacheManager;
//    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil springUtil = applicationContext.getBean(SpringUtil.class);
        springUtil.setApplicationContext(applicationContext);
    }
}
