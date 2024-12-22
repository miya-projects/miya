package com.miya.system.config.swagger.customizer;

import cn.hutool.core.util.ReflectUtil;
import com.miya.common.module.specificationbinds.EntityBinder;
import com.miya.common.module.specificationbinds.QueryBindingConfigurator;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.PrimitiveType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.JavadocProvider;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class SpecificationPredicateOperationCustomizer implements GlobalOperationCustomizer {


    /**
     * The constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationPredicateOperationCustomizer.class);

    private final JavadocProvider javadocProvider;

    private final QueryBindingConfigurator configurator;


    /**
     * The Spring doc config properties.
     */
    private final SpringDocConfigProperties springDocConfigProperties;

    /**
     * Instantiates a new Querydsl predicate operation customizer.
     * @param springDocConfigProperties the spring doc config properties
     */
    public SpecificationPredicateOperationCustomizer(JavadocProvider javadocProvider,
                                                     SpringDocConfigProperties springDocConfigProperties, QueryBindingConfigurator configurator) {
        this.springDocConfigProperties = springDocConfigProperties;
        //this.javadocProvider = SpringUtil.getBean(JavadocProvider.class);
        this.javadocProvider = javadocProvider;
        this.configurator = configurator;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();

        int parametersLength = methodParameters.length;
        List<Parameter> parametersToAddToOperation = new ArrayList<>();

        EntityBinder binder = configurator.getBindingsForEntity(handlerMethod.getMethod().getClass());

        for (int i = 0; i < parametersLength; i++) {
            MethodParameter parameter = methodParameters[i];
            if (!parameter.getParameterType().equals(Specification.class)) {
                continue;
            }
            ParameterizedType parameterizedType = (ParameterizedType)parameter.getGenericParameterType();
            Class<?> type = (Class<?>)parameterizedType.getActualTypeArguments()[0];

            Set<String> fieldsToAdd = Arrays.stream(type.getDeclaredFields()).filter(field -> !Modifier.isStatic(field.getModifiers())).map(Field::getName).collect(Collectors.toSet());
            for (String fieldName : fieldsToAdd) {
                if (!binder.shouldBind(fieldName)) {
                    continue;
                }
                Type filedType = getFieldType(fieldName, type);
                Parameter newParameter = buildParam(type, filedType, fieldName);
                parametersToAddToOperation.add(newParameter);
            }

        }
        if (!CollectionUtils.isEmpty(parametersToAddToOperation)) {
            if (operation.getParameters() == null)
                operation.setParameters(parametersToAddToOperation);
            else
                operation.getParameters().addAll(parametersToAddToOperation);
        }
        return operation;
    }

    /***
     * Tries to figure out the Type of the field. It first checks the Qdsl pathSpecMap before checking the root class. Defaults to String.class
     * @param fieldName The name of the field used as reference to get the type
     * @param root The root type where the paths are gotten
     * @return The type of the field. Returns
     */
    private Type getFieldType(String fieldName, Class<?> root) {
        Type genericType = null;
        try {
            Field declaredField;
            declaredField = root.getDeclaredField(fieldName);
            genericType = declaredField.getGenericType();
        }
        catch (NoSuchFieldException e) {
            LOGGER.warn("Field {} not found on {} : {}", fieldName, root.getName(), e.getMessage());
        }
        return genericType;
    }


    /***
     * Constructs the parameter
     * @param type The type of the parameter
     * @param name The name of the parameter
     * @return The swagger parameter
     */
    private Parameter buildParam(Class<?> root, Type type, String name) {
        Parameter parameter = new Parameter();

        if (StringUtils.isBlank(parameter.getName())) {
            parameter.setName(name);
        }

        if (StringUtils.isBlank(parameter.getIn())) {
            parameter.setIn("query");
        }

        if (parameter.getSchema() == null) {
            Schema<?> schema;
            schema = PrimitiveType.fromType(String.class).createProperty();
            PrimitiveType primitiveType = PrimitiveType.fromType(type);
            if (primitiveType != null) {
                schema = primitiveType.createProperty();
            }
            else {
                ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                        .resolveAsResolvedSchema(
                                new io.swagger.v3.core.converter.AnnotatedType(type).resolveAsRef(true));
                // could not resolve the schema or this schema references other schema
                // we dont want this since there's no reference to the components in order to register a new schema if it doesnt already exist
                // defaulting to string
                if (resolvedSchema == null || !resolvedSchema.referencedSchemas.isEmpty()) {
                    if (resolvedSchema != null && resolvedSchema.schema instanceof ArraySchema) {
                        //schema = resolvedSchema.schema;
                        ArraySchema arraySchema = new ArraySchema();
                        arraySchema.items(PrimitiveType.fromType(String.class).createProperty());
                        arraySchema.type("array");
                        schema = arraySchema;
                    }
                }
                else {
                    schema = resolvedSchema.schema;
                }
            }

            Field field = ReflectUtil.getField(root, name);
            String fieldJavadoc = this.javadocProvider.getFieldJavadoc(field);
            if (fieldJavadoc != null) {
                parameter.setDescription(fieldJavadoc);
            }
            parameter.setSchema(schema);
        }
        return parameter;
    }
}
