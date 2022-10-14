package com.miya.system.config.swagger.plugin;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

//todo

/**
 * 为api接口合理命名
 */
@Component
@Order
public class CustomOperationNicknameReader implements OperationBuilderPlugin {
    @Override
    public void apply(OperationContext context) {
        //Create your own transformation to format the name in the way
        //that you prefer
        String operationNameStem = transformName(context.getName());
        //Update the method name stem that is used to generate a unique id
        context.operationBuilder().codegenMethodNameStem(operationNameStem);
    }

    private String transformName(String name) {
        return name;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
