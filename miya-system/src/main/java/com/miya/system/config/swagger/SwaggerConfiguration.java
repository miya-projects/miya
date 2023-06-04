package com.miya.system.config.swagger;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.miya.system.config.swagger.customizer.*;
import com.miya.system.config.web.ReadableEnum;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.configuration.SpringDocDataRestConfiguration;
import org.springdoc.core.converters.PageableOpenAPIConverter;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.MediaType;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.utils.SpringDocUtils.getConfig;

/**
 * @author 杨超辉
 * 丝袜哥配置
 */
@Configuration
@AutoConfigureBefore(value = {SpringDocDataRestConfiguration.class})
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
public class SwaggerConfiguration {

    static {
//        getConfig().replaceParameterObjectWithClass(Timestamp.class, Date.class);
//        getConfig().replaceParameterObjectWithClass(YearMonth.class, String.class);
        getConfig().replaceWithClass(YearMonth.class, String.class);
        getConfig().replaceWithClass(Timestamp.class, Date.class);
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = false;
        ModelConverters.getInstance().addConverter(new GenericModelConverter());
//        ModelConverters.getInstance().addConverter(new ReadEnumModelConverter());
    }


    /**
     * 自定义全局配置openapi
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                                .addSecuritySchemes("jwt",
                                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
//                        .addParameters("myHeader1", new Parameter().in("header").schema(new StringSchema()).name("myHeader1"))
//                        .addHeaders("myHeader2", new Header().description("myHeader2 header").schema(new StringSchema()))
                )
                .addSecurityItem(new SecurityRequirement().addList("jwt"))
                .info(new Info().title("miya").version("1.0").description("miya description"))
                ;
    }

    /**
     * 编程式配置springdoc属性
     */
    @Bean
    SpringDocConfigProperties springDocConfigProperties() {
        SpringDocConfigProperties properties = new SpringDocConfigProperties();
        properties.setDefaultConsumesMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        properties.setDefaultProducesMediaType(MediaType.APPLICATION_JSON_VALUE);
        properties.setEnableSpringSecurity(true);
        return properties;
    }

    @Bean
    @ConditionalOnClass(DomainClassConverter.class)
    @Lazy(false)
    DomainClassGlobalSupport domainClassGlobalOperationCustomizer() {
        Repositories repositories = new Repositories(SpringUtil.getApplicationContext());
        ArrayList<Class<?>> domains = ListUtil.toList(repositories);
        getConfig()
                .addRequestWrapperToIgnore(domains.toArray(new Class<?>[0]));
        return new DomainClassGlobalSupport(repositories);
    }

    /**
     * Pageable参数文档转换
     */
    @Bean
    @Lazy(false)
    PageableOpenAPIConverter pageableOpenAPIConverter(ObjectMapperProvider objectMapperProvider) {
        getConfig().replaceParameterObjectWithClass(org.springframework.data.domain.Pageable.class, Page.class)
                .replaceParameterObjectWithClass(org.springframework.data.domain.PageRequest.class, Page.class);
        return new PageableOpenAPIConverter(objectMapperProvider);
    }

    /**
     * 解析querydsl的predicate参数
     */
    @Bean
    QuerydslPredicateOperationWithJavaDocCustomizer queryDslQuerydslPredicateOperationCustomizer(
            Optional<QuerydslBindingsFactory> querydslBindingsFactory, JavadocProvider javadocProvider) {
        if (querydslBindingsFactory.isPresent()) {
            getConfig().addRequestWrapperToIgnore(Predicate.class);
            return new QuerydslPredicateOperationWithJavaDocCustomizer(querydslBindingsFactory.get(), javadocProvider);
        }
        return null;
    }

    @Bean
    ExtClientMethodNameSupport ExtClientMethodNameReader() {
        return new ExtClientMethodNameSupport();
    }

//    @Bean
    GenericReturnTypeSupport genericReturnTypeSupport() {
        return new GenericReturnTypeSupport(springDocConfigProperties().getDefaultProducesMediaType());
    }

    @Bean
    PropertyCustomizer propertyCustomizer() {
        return new PropertyCustomizer() {

            @Override
            public Schema customize(Schema property, AnnotatedType type) {
                if (type.isSchemaProperty()) {
//                    if (type.getType().isEnum()) {
//                        property.setEnum(ListUtil.toList(type.getType().getEnumConstants()));
//                    }
                }
                return property;
            }
        };
    }

//    @Bean
//    public GlobalOpenApiCustomizer extraApi() {
//        return new GlobalOpenApiCustomizer() {
//
//            @Override
//            public void customise(OpenAPI openApi) {
//                Paths paths = openApi.getPaths();
//                PathItem pathItem = new PathItem();
//                pathItem.operation();
//                paths.addPathItem("1",)
//            }
//        };
//    }

}
