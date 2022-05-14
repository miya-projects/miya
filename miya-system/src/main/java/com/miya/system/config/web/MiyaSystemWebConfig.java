package com.miya.system.config.web;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.miya.common.config.web.interceptor.SignAccessInterceptor;
import com.miya.common.module.base.ReadableEnum;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.filter.interceptors.ApiAccessInterceptor;
import com.miya.system.config.filter.interceptors.EscapeSensitiveWordFilter;
import com.miya.system.module.FlagForMiyaSystemModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.TimeZone;

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
        registry.addInterceptor(new EscapeSensitiveWordFilter())
                .addPathPatterns(Collections.singletonList("/api/**"));

    }


    /**
     * 扩展jackson配置
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
}
