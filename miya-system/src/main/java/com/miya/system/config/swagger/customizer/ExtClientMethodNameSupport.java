package com.miya.system.config.swagger.customizer;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.web.method.HandlerMethod;

/**
 * 为前端添加一个合理的方法名，避免使用自动生成的list1、list2等
 */
public class ExtClientMethodNameSupport implements GlobalOperationCustomizer {

    private static final String EXTENSION_KEY = "x-client-method";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        String name = handlerMethod.getMethod().getName();
        operation.addExtension(EXTENSION_KEY, name);
        return operation;
    }
}
