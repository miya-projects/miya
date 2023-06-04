package com.miya.system.config.swagger.customizer;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Iterator;

// todo
public class ReadEnumModelConverter implements ModelConverter {
    @Override
    public Schema resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }
//        if (annotatedType.getType() instanceof Class<?> c) {
//            if (c.isEnum()) {
//                if (ReadableEnum.class.isAssignableFrom(c)) {
//                    AnnotatedType newAnnotatedType = new AnnotatedType(DropDownItemDTO.class).jsonViewAnnotation(annotatedType.getJsonViewAnnotation()).resolveAsRef(true);
//                    newAnnotatedType.setName(annotatedType.getName());
//                    return chain.next().resolve(newAnnotatedType, context, chain);
//                }
//            }
//        }
        return chain.next().resolve(annotatedType, context, chain);
    }
}
