package com.miya.system.config.swagger.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;
import springfox.documentation.spring.web.readers.parameter.ParameterTypeReader;

/**
 * 展开参数参数类型读取插件，@RequestBody类型的参数，不需要展开，不会走这个插件
 */
@Order
@Component
public class CustomExpandedParameterTypeReader implements ExpandedParameterBuilderPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTypeReader.class);


    @Override
    public void apply(ParameterExpansionContext context) {
        context.getParameterBuilder().parameterType("formData");
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
