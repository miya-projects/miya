package com.miya.system.config.swagger.plugin;


import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.miya.common.module.base.BaseEntity;
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
import springfox.documentation.schema.Enums;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.AllowableValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.EnumTypeDeterminer;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static springfox.documentation.schema.Collections.*;
import static springfox.documentation.schema.Collections.isContainerType;
import static springfox.documentation.schema.Types.typeNameFor;
import static springfox.documentation.service.Parameter.DEFAULT_PRECEDENCE;

/**
 * 解决 @QuerydslPredicate 注解 root 为实体类的情况的 swagger 参数显示和调试问题
 */
@Slf4j
@RequiredArgsConstructor
public class QuerydslPredicateReader implements OperationBuilderPlugin {

    private final TypeResolver resolver;

    private final QuerydslBindingsFactory querydslBindingsFactory;

    private final EnumTypeDeterminer enumTypeDeterminer;

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    /**
     * 找到带有 QuerydslPredicate 注解的参数，然后获取root的类型，对里面的属性进行处理
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
                    if (invoke == null) {
                        continue;
                    }
                    // 为了 ModelRef
                    Class<?> type = declaredField.getType();
                    if (BaseEntity.class.isAssignableFrom(type)) {
                        // 实体类传参等于使用ID
                        type = String.class;
                    }

                    ResolvedType resolvedType = resolver.resolve(type);

                    // 解析参数
                    ParameterBuilder parameterBuilder = extracted(name, resolvedType.getErasedType(), resolvedType, context);
                    if (parameterBuilder == null) {
                        // map jsonnode 不支持
                        continue;
                    }

                    // 描述
                    String description;
                    // 是否必填
                    boolean required;
                    ApiModelProperty apiModelProperty = declaredField.getAnnotation(ApiModelProperty.class);
                    final Optional<ApiModelProperty> propertyOptional = Optional.ofNullable(apiModelProperty);

                    description = propertyOptional.map(ApiModelProperty::value).orElse("querydsl自动生成");
                    required = propertyOptional.map(ApiModelProperty::required).orElse(false);

                    parameterBuilder.required(required)
                            .description(description);
                    parameterList.add(parameterBuilder.build());
                }
            });
        }

        context.operationBuilder().parameters(parameterList);
    }

    private ParameterBuilder extracted(String name, Class<?> erasedType, ResolvedType resolved, OperationContext context) {
        String typeName = typeNameFor(resolved.getErasedType());

        AllowableValues allowable = allowableValues(erasedType);

        ModelReference itemModel = null;
        if (isContainerType(resolved)) {
            ResolvedType elementType = collectionElementType(resolved);
            String itemTypeName = typeNameFor(elementType.getErasedType());
            AllowableValues itemAllowables = null;
            if (enumTypeDeterminer.isEnum(elementType.getErasedType())) {
                itemAllowables = Enums.allowableValues(elementType.getErasedType());
                itemTypeName = "string";
            }
            typeName = containerType(resolved);
            itemModel = new ModelRef(itemTypeName, itemAllowables);
        } else if (enumTypeDeterminer.isEnum(resolved.getErasedType())) {
            typeName = "string";
        }
        if (typeName == null) {
            return null;
        }
        return new ParameterBuilder()
                .name(name)
                .defaultValue(null)
                .allowMultiple(isContainerType(resolved))
                .type(resolved)
                .modelRef(new ModelRef(typeName, itemModel))
                .allowableValues(allowable)
                .parameterType("query")
                .order(DEFAULT_PRECEDENCE)
                .parameterAccess(null);
    }

    private AllowableValues allowableValues(Class<?> fieldType) {

        AllowableValues allowable = null;
        if (enumTypeDeterminer.isEnum(fieldType)) {
            allowable = new AllowableListValues(getEnumValues(fieldType), "LIST");
        }

        return allowable;
    }

    private List<String> getEnumValues(final Class<?> subject) {
        return Stream.of(subject.getEnumConstants())
                .map((Function<Object, String>) Object::toString)
                .collect(toList());
    }

}

