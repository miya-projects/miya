package com.miya.system.config.swagger.plugin;

import com.fasterxml.classmate.ResolvedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.spring.web.readers.parameter.ParameterTypeReader;

import java.util.Set;

import static springfox.documentation.schema.Collections.collectionElementType;
import static springfox.documentation.schema.Collections.isContainerType;

/**
 * 自定义解析paramType插件，默认返回query
 */
@Order
@Component
public class CustomParameterTypeReader extends ParameterTypeReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTypeReader.class);

    @Override
    public void apply(ParameterContext context) {
        context.parameterBuilder().parameterType(findParameterType(context));
    }

    public static String findParameterType(ParameterContext parameterContext){
        ResolvedMethodParameter resolvedMethodParameter = parameterContext.resolvedMethodParameter();
        ResolvedType parameterType = resolvedMethodParameter.getParameterType();
        parameterType = parameterContext.alternateFor(parameterType);
        //Multi-part file trumps any other annotations
        if (isFileType(parameterType) || isListOfFiles(parameterType)) {
            return "form";
        }
        if (resolvedMethodParameter.hasParameterAnnotation(PathVariable.class)) {
            return "path";
        } else if (resolvedMethodParameter.hasParameterAnnotation(RequestBody.class)) {
            return "body";
        } else if (resolvedMethodParameter.hasParameterAnnotation(RequestPart.class)) {
            return "formData";
        } else if (resolvedMethodParameter.hasParameterAnnotation(RequestParam.class)) {
            return determineScalarParameterType(
                    parameterContext.getOperationContext().consumes(),
                    parameterContext.getOperationContext().httpMethod());
        } else if (resolvedMethodParameter.hasParameterAnnotation(RequestHeader.class)) {
            return "header";
        } else if (resolvedMethodParameter.hasParameterAnnotation(ModelAttribute.class)) {
            LOGGER.warn("@ModelAttribute annotated parameters should have already been expanded via "
                    + "the ExpandedParameterBuilderPlugin");
        }
        if (!resolvedMethodParameter.hasParameterAnnotations()) {
            return determineScalarParameterType(
                    parameterContext.getOperationContext().consumes(),
                    parameterContext.getOperationContext().httpMethod());
        }
        return "form";


    }

    private static String determineScalarParameterType(Set<? extends MediaType> consumes, HttpMethod method) {
        // String parameterType = "query";
        String parameterType = "form";

        if (consumes.contains(MediaType.APPLICATION_FORM_URLENCODED)
                && method == HttpMethod.POST) {
            parameterType = "form";
        } else if (consumes.contains(MediaType.MULTIPART_FORM_DATA)
                && method == HttpMethod.POST) {
            parameterType = "formData";
        }

        return parameterType;
    }

    private static boolean isListOfFiles(ResolvedType parameterType) {
        return isContainerType(parameterType) && isFileType(collectionElementType(parameterType));
    }

    private static boolean isFileType(ResolvedType parameterType) {
        return MultipartFile.class.isAssignableFrom(parameterType.getErasedType());
    }
}
