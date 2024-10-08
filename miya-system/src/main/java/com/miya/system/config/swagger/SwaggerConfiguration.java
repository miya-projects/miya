package com.miya.system.config.swagger;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.swagger.customizer.DomainClassGlobalSupport;
import com.miya.system.config.swagger.customizer.ExtClientMethodNameSupport;
import com.miya.system.config.swagger.customizer.QuerydslPredicateOperationWithJavaDocCustomizer;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.configuration.SpringDocDataRestConfiguration;
import org.springdoc.core.converters.PageableOpenAPIConverter;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.DomainClassConverter;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.utils.SpringDocUtils.getConfig;

/**
 * @author 杨超辉
 * 丝袜哥配置
 */
@Slf4j
@Configuration
@AutoConfigureBefore(value = {SpringDocDataRestConfiguration.class})
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
public class SwaggerConfiguration implements ApplicationRunner {

    static {
//        getConfig().replaceParameterObjectWithClass(Timestamp.class, Date.class);
//        getConfig().replaceParameterObjectWithClass(YearMonth.class, String.class);
        getConfig().replaceWithClass(YearMonth.class, String.class);
        getConfig().replaceWithClass(Timestamp.class, Date.class);
        getConfig().addAnnotationsToIgnore(AuthenticationPrincipal.class);
        getConfig().addJavaTypeToIgnore(HttpServletRequest.class);
        getConfig().addJavaTypeToIgnore(HttpServletResponse.class);
        io.swagger.v3.core.jackson.ModelResolver.enumsAsRef = false;
//        ModelConverters.getInstance().addConverter(new GenericModelConverter());
//        ModelConverters.getInstance().addConverter(new ReadEnumModelConverter());
    }


    /**
     * 自定义全局配置openapi
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
            Optional<QuerydslBindingsFactory> querydslBindingsFactory, JavadocProvider javadocProvider,
            SpringDocConfigProperties springDocConfigProperties) {
        if (querydslBindingsFactory.isPresent()) {
            getConfig().addRequestWrapperToIgnore(Predicate.class);
            return new QuerydslPredicateOperationWithJavaDocCustomizer(querydslBindingsFactory.get(), javadocProvider, springDocConfigProperties);
        }
        return null;
    }

    @Bean
    ExtClientMethodNameSupport ExtClientMethodNameReader() {
        return new ExtClientMethodNameSupport();
    }

    //@Bean
    //PropertyCustomizer propertyCustomizer() {
    //    return new PropertyCustomizer() {
    //
    //        @Override
    //        public Schema customize(Schema property, AnnotatedType type) {
    //            if (type.isSchemaProperty()) {
    //                if (type.getType().isEnum()) {
    //                    property.setEnum(ListUtil.toList(type.getType().getEnumConstants()));
    //                }
    //            }
    //            return property;
    //        }
    //    };
    //}

    @Override
    public void run(ApplicationArguments args) {
        ProjectConfiguration configuration = SpringUtil.getBean(ProjectConfiguration.class);
        if (!configuration.isProduction()) {
            Environment environment = SpringUtil.getBean(Environment.class);
            String port = environment.getProperty("server.port");

            LinkedHashSet<InetAddress> inetAddresses = NetUtil.localAddressList(inter -> !inter.getDisplayName().contains("Virtual"), add -> add instanceof Inet4Address);
            for (InetAddress ip : inetAddresses) {
                String url = StrUtil.format("http://{}:{}/swagger-ui.html", ip.getHostAddress(), port);
                log.info("swagger地址: {}", url);
            }
        }

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
