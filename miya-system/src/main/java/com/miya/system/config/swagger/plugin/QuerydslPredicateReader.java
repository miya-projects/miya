package com.miya.system.config.swagger.plugin;


import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.util.CastUtils;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static springfox.documentation.schema.Types.isBaseType;
import static springfox.documentation.schema.Types.typeNameFor;

/**
 * 解决 @QuerydslPredicate 注解 root 为实体类的情况的 swagger 参数显示和调试问题.注意目前为了简单，属性上需要加上 Swagger 的 ApiModelProperty 注解
 *
 * @author hansai
 */
@Slf4j
@RequiredArgsConstructor
public class QuerydslPredicateReader implements OperationBuilderPlugin {

    private final TypeResolver resolver;

    private final QuerydslBindingsFactory querydslBindingsFactory;

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    /**
     * 找到带有 QuerydslPredicate 注解的参数，然后获取root的类型，对里面的属性进行处理
     *
     * @param context
     */
    @Override
    public void apply(OperationContext context) {
        List<ResolvedMethodParameter> resolvedMethodParameters = context.getParameters();
        List<Parameter> parameterList = new ArrayList<>();
        for (ResolvedMethodParameter resolvedMethodParameter : resolvedMethodParameters) {
            Optional<QuerydslPredicate> optionalQuerydslPredicate = resolvedMethodParameter.findAnnotation(QuerydslPredicate.class);
            optionalQuerydslPredicate.ifPresent(querydslPredicate -> {
                final Class<?> root = querydslPredicate.root();
                final Class<? extends QuerydslBinderCustomizer> bindingsClass = querydslPredicate.bindings();
                final TypeInformation<?> domainType = ClassTypeInformation.from(root);
                QuerydslBindings bindings = querydslBindingsFactory.createBindingsFor(domainType, CastUtils.cast(bindingsClass));

                Field[] declaredFields = ReflectUtil.getFields(root);
                for (Field declaredField : declaredFields) {
                    String name = declaredField.getName();
                    final Method getPropertyPath = ReflectUtil.getMethodByName(QuerydslBindings.class, "getPropertyPath");
                    getPropertyPath.setAccessible(true);
                    Object invoke = null;
                    try {
                        invoke = getPropertyPath.invoke(bindings, name, domainType);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (invoke == null){
                        continue;
                    }
                    // 为了 ModelRef
                    Class<?> type = declaredField.getType();
                    ModelRef modelRef = null;
                    ResolvedType resolvedType = resolver.resolve(type);
                    ResolvedType alternateFor = context.alternateFor(resolvedType);
                    String typeName = typeNameFor(alternateFor.getErasedType());
                    if (isBaseType(typeName)) {
                        modelRef = new ModelRef(typeName);
                    }

                    // 描述
                    String description;
                    // 是否必填
                    boolean required;
                    ApiModelProperty apiModelProperty = declaredField.getAnnotation(ApiModelProperty.class);
                    final Optional<ApiModelProperty> propertyOptional = Optional.ofNullable(apiModelProperty);

                    description = propertyOptional.map(ApiModelProperty::value).orElse("querydsl自动生成");
                    required = propertyOptional.map(ApiModelProperty::required).orElse(false);

                    Parameter parameter = new ParameterBuilder()
                            .name(name)
                            .description(description)
                            .parameterType("query")
                            .required(required)
                            .type(resolvedType)
                            // todo 这里需要优化和测试
                            .modelRef(Optional.ofNullable(modelRef).orElse(new ModelRef("#/definitions/SysFile")))
                            .build();

                    parameterList.add(parameter);
                }
            });
        }

        context.operationBuilder().parameters(parameterList);
    }

}

