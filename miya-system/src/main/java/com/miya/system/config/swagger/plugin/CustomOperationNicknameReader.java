package com.miya.system.config.swagger.plugin;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 为api接口合理命名
 */
@Component
@Order
@Slf4j
public class CustomOperationNicknameReader implements OperationBuilderPlugin {

    Pattern pattern = Pattern.compile("^(.+)(?:Api|Controller)$");

    @Override
    public void apply(OperationContext context) {
        // 重命名tag
        String name = StrUtil.toCamelCase(context.getGroupName().replaceAll("-", "_"));
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
            name = name.substring(0, name.lastIndexOf("Api"));
        }

        // 重命名operateId
        String operationNameStem = transformName(name + StrUtil.upperFirst(context.getName()));
        context.operationBuilder().codegenMethodNameStem(operationNameStem);
    }

    private String transformName(String name) {
        return StrUtil.toSymbolCase(name, '-');
        // return name;
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
