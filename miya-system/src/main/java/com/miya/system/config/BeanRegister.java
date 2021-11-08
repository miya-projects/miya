package com.miya.system.config;

import cn.hutool.core.date.DatePattern;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.miya.system.listener.event.SystemStartupEvent;
import com.miya.common.module.base.ReadableEnum;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.concurrent.DelegatingSecurityContextScheduledExecutorService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 注册bean都在这
 */
@Lazy(false)
@Configuration
@Slf4j
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class BeanRegister implements ApplicationContextAware {

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * hash密码 bean
     * @return
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DelegatingSecurityContextScheduledExecutorService delegatingSecurityContextScheduledExecutorService() {
        ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(5);
        return new DelegatingSecurityContextScheduledExecutorService(scheduledExecutorService);
    }

    /**
     * 扩展jackson配置
     * @return
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(){
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.simpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
            jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
            jacksonObjectMapperBuilder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            jacksonObjectMapperBuilder.serializerByType(ReadableEnum.class, new JsonSerializer<ReadableEnum>() {
                @Override
                public void serialize(ReadableEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (!(value instanceof Enum)){
                        gen.writeString(value.getName());
                        log.warn("ReadableEnum应用在了非Enum类型上");
                        return;
                    }
                    String v = ((Enum<?>) value).name();
                    gen.writeStartObject();
                    gen.writeStringField("label", value.getName());
                    gen.writeStringField("value", v);
                    gen.writeEndObject();
                }
            });
            // jacksonObjectMapperBuilder.serializerByType(Business.class, new JsonSerializer<Business>() {
            //     @Override
            //     public void serialize(Business value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            //         gen.writeStartObject();
            //         gen.writeStringField("name", value.getName());
            //         gen.writeStringField("code", value.getCode());
            //         if (value instanceof BusinessNode){
            //             gen.writeObjectField("children", ((BusinessNode)value).getChildren());
            //         }else {
            //             gen.writeObjectField("children", Collections.EMPTY_LIST);
            //         }
            //         gen.writeEndObject();
            //     }
            // });
        };
    }

    // @Bean
    // public Converter<SysUser, SysUserDTO> converter(ConverterRegistry converterRegistry){
    //     Converter<SysUser, SysUserDTO> converter = new Converter<SysUser, SysUserDTO>() {
    //         @Override
    //         public SysUserDTO convert(SysUser sysUser) {
    //             SysUserDTO sysUserDTO = new SysUserDTO();
    //             sysUserDTO.setName(sysUser.getName());
    //             return sysUserDTO;
    //         }
    //     };
    //     converterRegistry.addConverter(converter);
    //     return converter;
    // }

    @Bean
    protected AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> authenticationProviders = new ArrayList<>();
        authenticationProviders.add(new AnonymousAuthenticationProvider("anonymous"));
        ProviderManager authenticationManager = new ProviderManager(authenticationProviders);
        //不擦除认证密码，擦除会导致TokenBasedRememberMeServices因为找不到Credentials再调用UserDetailsService而抛出UsernameNotFoundException
        authenticationManager.setEraseCredentialsAfterAuthentication(false);
        return authenticationManager;
    }

    /**
     * 应用启动监听器
     * @return
     */
    @Bean
    public ApplicationRunner builds(ApplicationContext applicationContext){
        return (args) -> new Thread(() -> applicationContext.publishEvent(new SystemStartupEvent())).start();
    }

    /**
     * 分页从1开始?
     * @return
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

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil springUtil = applicationContext.getBean(SpringUtil.class);
        springUtil.setApplicationContext(applicationContext);
    }
}
