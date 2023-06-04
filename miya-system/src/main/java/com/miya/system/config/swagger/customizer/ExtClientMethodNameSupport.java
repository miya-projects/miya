package com.miya.system.config.swagger.customizer;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 为前端添加一个合理的方法名，避免使用自动生成的list1、list2等
 */
public class ExtClientMethodNameSupport implements GlobalOperationCustomizer {

    private static final String EXTENSION_KEY = "x-client-method";
    Pattern pattern = Pattern.compile("^(.+)(?:Api|Controller)$");

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        String methodName = handlerMethod.getMethod().getName();
        operation.addExtension(EXTENSION_KEY, methodName);
        renameOperationId(operation, handlerMethod);
        return operation;
    }

    private void renameOperationId(Operation operation, HandlerMethod handlerMethod) {
        // 重命名tag
        String name = null;
//        if (operation.getTags().size() > 0) {
//            name = StrUtil.toCamelCase(operation.getTags().get(0).replaceAll("-", "_"));
//        }
        name = handlerMethod.getBeanType().getSimpleName();
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            try {
                name = name.substring(0, name.lastIndexOf("Api"));
            }catch (StringIndexOutOfBoundsException e) {
                name = name.substring(0, name.lastIndexOf("Controller"));
            }
        }
        // 重命名operateId
        operation.setOperationId(transformName(name + StrUtil.upperFirst(operation.getOperationId())));
    }

    private String transformName(String name) {
        return StrUtil.toSymbolCase(name, '-');
        // return name;
    }
}
