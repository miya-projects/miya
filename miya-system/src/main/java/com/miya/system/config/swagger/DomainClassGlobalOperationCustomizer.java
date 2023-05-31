package com.miya.system.config.swagger;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

@Slf4j
@RequiredArgsConstructor
public class DomainClassGlobalOperationCustomizer implements GlobalOperationCustomizer {
    private final Repositories repositories;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();

        for (MethodParameter methodParameter : methodParameters) {
            if (!repositories.hasRepositoryFor(methodParameter.getParameterType())) {
                continue;
            }
            PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
            RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
            String parameterName = methodParameter.getParameterName();
            if (pathVariable != null) {
                parameterName = pathVariable.name();
            }
            if (requestParam != null) {
                parameterName = requestParam.name();
            }
            if (StrUtil.isBlank(parameterName)) {
                log.warn("参数名为空，无法获取参数名，跳过该参数");
                continue;
            }

            Parameter parameter = new Parameter();
            parameter.setName(parameterName);
            if (pathVariable != null) {
                parameter.setIn("path");
            } else if (requestParam != null) {
                parameter.setIn("query");
            }

            RepositoryInformation information = repositories.getRequiredRepositoryInformation(methodParameter.getParameterType());
            TypeDescriptor idTypeDescriptor = information.getIdTypeInformation().toTypeDescriptor();
            Schema<?> schema;
            PrimitiveType primitiveType = PrimitiveType.fromType(idTypeDescriptor.getType());
            schema = primitiveType.createProperty();
            parameter.setSchema(schema);
            operation.getParameters().removeIf(p -> {
                return p.getIn().equals(parameter.getIn()) && p.getName().equals(parameter.getName());
            });
            operation.getParameters().add(parameter);
        }
        return operation;
    }
}
