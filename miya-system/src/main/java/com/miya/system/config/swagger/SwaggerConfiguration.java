package com.miya.system.config.swagger;


import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.classmate.TypeResolver;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.miya.common.annotation.Acl;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.module.FlagForMiyaSystemModule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRuleConvention;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

/**
 * @author ?????????
 * ???????????????
 */
@Configuration
// @EnableWebMvc
@EnableSwagger2WebMvc
@EnableKnife4j
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Resource
    private ProjectConfiguration projectConfiguration;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private Map<String, DocketBuilder> beansOfType;

    /**
     * ??????DocketBuilder??????docket
     */
    @PostConstruct
    public void buildDocket() {
        for (DocketBuilder docketBuilder : beansOfType.values()) {
            Docket docket = createDocket(docketBuilder.getTitle(), docketBuilder.getPredicate());
            DefaultListableBeanFactory fty = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            fty.registerSingleton(docketBuilder.getTitle(), docket);
        }
    }

    /**
     * ??????docket??????swagger-resources
     * @param title
     * @param predicate
     */
    public Docket createDocket(String title, Predicate<RequestHandler> predicate) {
        return new Docket(DocumentationType.SWAGGER_2)
                //?????????????????????
                .enable(!projectConfiguration.isProduction())
                .apiInfo(new ApiInfoBuilder()
                        .title(title)
                        // .contact(new Contact(configService.get(""), "https://rxxy.github.io", ""))
                        .version(projectConfiguration.getVersion())
                        .build())
                .groupName(title)
                .ignoredParameterTypes(
                        AuthenticationPrincipal.class,
                        RequestAttribute.class,
                        SessionAttribute.class,
                        HttpSession.class,
                        HttpServletResponse.class,
                        HttpServletRequest.class
                )
                .enableUrlTemplating(true)
                .globalOperationParameters(globalOperationParameters())
                .select()
                .apis(predicate)
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * ????????????
     */
    private List<Parameter> globalOperationParameters(){
        return Arrays.asList(new ParameterBuilder()
                .name("Authorization")
                .description("??????jwt token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .defaultValue("Bearer ")
                .required(false)
                .build());
    }

    // @Bean
    // UiConfiguration uiConfig() {
    //     return UiConfigurationBuilder.builder()
    //             .deepLinking(true)
    //             .displayOperationId(false)
    //             .defaultModelsExpandDepth(1)
    //             .defaultModelExpandDepth(1)
    //             .defaultModelRendering(ModelRendering.EXAMPLE)
    //             .displayRequestDuration(false)
    //             .docExpansion(DocExpansion.NONE)
    //             .filter(false)
    //             .maxDisplayedTags(null)
    //             .operationsSorter(OperationsSorter.ALPHA)
    //             .showExtensions(false)
    //             .showCommonExtensions(false)
    //             .tagsSorter(TagsSorter.ALPHA)
    //             .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
    //             .validatorUrl(null)
    //             .build();
    // }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        // registry.
        //         addResourceHandler("/swagger-ui/**")
        //         .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
        //         .resourceChain(false);
    }

    // @Override
    // public void addViewControllers(ViewControllerRegistry registry) {
    //     registry.addViewController("/swagger-ui/")
    //             .setViewName("forward:/swagger-ui/index.html");
    // }


    /**
     * API?????????
     */
    @RequiredArgsConstructor
    public static class ApiSelectors {
        /**
         * ???????????????????????????????????????????????????
         * @param userType
         */
        public static Predicate<RequestHandler> userTypeApiSelector(Class<?> userType) {
            Class[] classes = new Class[]{userType, Acl.AllUser.class, Acl.NotNeedLogin.class};
            //??????????????????????????????????????? eg: apis(isBackendApi)
            return input -> {
                Optional<Acl> methodAclOptional = input.findAnnotation(Acl.class);
                Optional<Acl> controllerAclOptional = input.findControllerAnnotation(Acl.class);
                //??????class???method???????????????@Acl
                boolean isNoConfig = !(methodAclOptional.isPresent() || controllerAclOptional.isPresent());
                if (isNoConfig) {
                    return true;
                }
                // ????????????controller????????????
                if (controllerAclOptional.isPresent()) {
                    return Arrays.asList(classes).contains(controllerAclOptional.get().userType());
                }
                return Arrays.asList(classes).contains(methodAclOptional.get().userType());
                //???????????????method???class?????????@Acl????????????method > class????????????????????????
                //            if (methodAclOptional.isPresent()) {
                //                return Arrays.asList(classes).contains(methodAclOptional.get().userType());
                //            }else {
                //                return Arrays.asList(classes).contains(controllerAclOptional.get().userType());
                //            }
            };
        }

        /**
         * ????????????????????????????????????
         * @param packageName
         */
        public static Predicate<RequestHandler> packageApiSelector(String packageName) {
            return input -> {
                return input.getHandlerMethod().getBeanType().getPackage().getName().startsWith(packageName);
            };
        }

        /**
         * ?????????????????????????????????????????????????????????
         * @param userType
         * @param packageName
         */
        public static Predicate<RequestHandler> userTypeAndPackageApiSelector(Class<?> userType, String packageName) {
            return packageApiSelector(packageName).and(userTypeApiSelector(userType));
        }

    }

    @Bean
    public QuerydslPredicateReader querydslPredicateReader(TypeResolver resolver, QuerydslBindingsFactory querydslBindingsFactory){
        return new QuerydslPredicateReader(resolver, querydslBindingsFactory);
    }

    /**
     * ??????Pageable??????????????????pageable?????????????????????
     */
    @Bean
    public AlternateTypeRuleConvention pageableConvention() {
        return new AlternateTypeRuleConvention() {
            @Override
            public int getOrder() {
                return Ordered.LOWEST_PRECEDENCE;
            }

            @Override
            public List<AlternateTypeRule> rules() {
                List<AlternateTypeRule> arrayList;
                arrayList = newArrayList(
                        newRule(Pageable.class, Page.class)
                );
                return arrayList;
            }
        };
    }

    /**
     * api???????????????????????????
     */
    @ApiModel
    @Data
    static class Page {
        @ApiModelProperty("???page???,???0????????????")
        private Integer page;

        @ApiModelProperty("??????????????????")
        private Integer size;

        @ApiModelProperty(value = "???????????????,??????:??????,[asc|desc]")
        private List<String> sort;
    }

}
