package com.miya.system.config.swagger.customizer;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.web.method.HandlerMethod;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 泛型返回类型支持
 * 在springdoc.remove-broken-reference-definitions=true的请情况下会有问题
 * <pre>
 *     @Bean
 *     GenericReturnTypeSupport genericReturnTypeSupport() {
 *         return new GenericReturnTypeSupport(springDocConfigProperties().getDefaultProducesMediaType());
 *     }
 * </pre>
 * @deprecated  {@see com.miya.system.config.swagger.customizer.GenericModelConverter}
 */
@Deprecated
@RequiredArgsConstructor
public class GenericReturnTypeSupport implements GlobalOperationCustomizer, GlobalOpenApiCustomizer {

    /** 引用的schema，准备定义到openapi中 */
    private final Map<String, Schema<?>> responseSchemas = new ConcurrentHashMap<>();

    /* 默认返回的mediaType */
    private final String defaultProducesMediaType;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Class<?> returnType = handlerMethod.getMethod().getReturnType();
        Type genericReturnType = handlerMethod.getMethod().getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            return operation;
        }
        ResolvedSchema baseRespSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(genericReturnType));
        Map<String, Schema> fieldsSchema = new LinkedHashMap<>();
        fieldsSchema.putAll(baseRespSchema.schema.getProperties());

        StringBuilder realSchemaName = new StringBuilder(returnType.getSimpleName());
        Type currentType = parameterizedType;

        // 循环判断泛型，兼容多层泛型
        while (currentType instanceof ParameterizedType currentParameterizedType) {
            currentType = currentParameterizedType.getActualTypeArguments()[0];
            if (currentType instanceof Class<?> c) {
                realSchemaName.append(c.getSimpleName());
            } else if (currentType instanceof ParameterizedType pt) {
                realSchemaName.append(((Class<?>) pt.getRawType()).getSimpleName());
            }
        }
        Schema<?> schema = new ObjectSchema().type("object")
                .properties(fieldsSchema)
                .name(realSchemaName.toString());
        responseSchemas.put(realSchemaName.toString(), schema);

        ApiResponse okResponse = operation.getResponses().get("200");
        Content content = okResponse.getContent();
        if (content == null) {
            return operation;
        }
        io.swagger.v3.oas.models.media.MediaType defaultMediaType = content.get(defaultProducesMediaType);
        defaultMediaType.getSchema().$ref(schema.getName());
        return operation;
    }

    @Override
    public void customise(OpenAPI openApi) {
        openApi.getComponents().getSchemas().putAll(responseSchemas);
    }
}
