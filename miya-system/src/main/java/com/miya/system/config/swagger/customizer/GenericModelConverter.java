package com.miya.system.config.swagger.customizer;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.modelmapper.internal.bytebuddy.ByteBuddy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * 针对泛型返回值，动态生成泛型类子类
 * 不指定泛型类name即可，暂不使用该类
 */
public class GenericModelConverter implements ModelConverter {

    @Override
    public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }
        if (!(annotatedType.getType() instanceof ParameterizedType parameterizedType)) {
            return chain.next().resolve(annotatedType, context, chain);
        }
        if (parameterizedType.getRawType() instanceof Class<?> c) {
            if (Iterable.class.isAssignableFrom(c)) {
                return chain.next().resolve(annotatedType, context, chain);
            }
        }
        StringBuilder realSchemaName = new StringBuilder(((Class<?>)((ParameterizedType)annotatedType.getType()).getRawType()).getSimpleName());
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

        Class<?> dynamicType = new ByteBuddy()
                .subclass(annotatedType.getType())
                .name(realSchemaName.toString())
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();
        AnnotatedType newAnnotatedType = new AnnotatedType(dynamicType).resolveAsRef(annotatedType.isResolveAsRef())
                .jsonViewAnnotation(annotatedType.getJsonViewAnnotation()).ctxAnnotations(annotatedType.getCtxAnnotations());
        return chain.next().resolve(newAnnotatedType, context, chain);
    }
}
