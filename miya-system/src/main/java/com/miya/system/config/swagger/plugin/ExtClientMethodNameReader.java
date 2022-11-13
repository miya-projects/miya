package com.miya.system.config.swagger.plugin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.CastUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.RequestMappingContext;
import springfox.documentation.spring.web.WebMvcRequestHandler;
import springfox.documentation.spring.web.readers.parameter.ParameterTypeReader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 添加扩展参数，为客户端生成api提供一个名字
 */
@Order
@Component
public class ExtClientMethodNameReader implements OperationBuilderPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTypeReader.class);

    @Override
    public void apply(OperationContext context) {
        RequestMappingContext requestContext = (RequestMappingContext)ReflectUtil.getFieldValue(context, "requestContext");
        WebMvcRequestHandler handler = (WebMvcRequestHandler)ReflectUtil.getFieldValue(requestContext, "handler");;
        String name = handler.getHandlerMethod().getMethod().getName();

        List<VendorExtension<String>> extensions = new ArrayList<>();
        VendorExtension<String> vendorExtension = new VendorExtension<String>() {
            @Override
            public String getName() {
                return "x-client-method";
            }

            @Override
            public String getValue() {
                return name;
            }
        };
        extensions.add(vendorExtension);

        // 根据现有的messageConverter推断支持的produces
        List<MediaType> result = new ArrayList<>();
        Collection<HttpMessageConverter> values = SpringUtil.getBeansOfType(HttpMessageConverter.class).values();
        for (HttpMessageConverter<?> converter : values) {
            result.addAll(converter.getSupportedMediaTypes(handler.getHandlerMethod().getMethod().getReturnType()));
        }
        context.operationBuilder()
                // .produces(CollUtil.newHashSet(result.stream().map(MimeType::toString).collect(Collectors.toSet())))
                .extensions(CastUtils.cast(extensions));
        context.operationBuilder()
                .consumes(CollUtil.newHashSet("application/x-www-form-urlencoded"));
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
