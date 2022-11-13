package com.miya.system.config.swagger;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.ApiDescriptionBuilder;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * api方式增加swagger接口
 */
@Component
public class SwaggerAddition implements ApiListingScannerPlugin {
    @Override
    public List<ApiDescription> apply(DocumentationContext documentationContext) {
        return new ArrayList<>(
                Arrays.asList(
                        new ApiDescriptionBuilder(
                                Ordering.compound(Lists.newArrayList((o1, o2) -> 1))
                        ).path("/api/logout").description("登出").hidden(false)
                                .groupName("系统")
                                .operations(
                                        Lists.newArrayList(
                                                new OperationBuilder(new CachingOperationNameGenerator())
                                                        .produces(Sets.newHashSet())
                                                        .method(HttpMethod.POST)
                                                        .summary("登出")
                                                        .notes("登出")//方法描述
                                                        .tags(Sets.newHashSet("用户"))
                                                        .build()
                                        )
                                )
                                .groupName("系统")
                                .build()
                ));
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return DocumentationType.SWAGGER_2.equals(documentationType);
    }
}
