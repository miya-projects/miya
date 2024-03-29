package com.miya.common.annotation.constraint;

import cn.hutool.core.collection.ListUtil;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 自定义消息变量插值器
 * 增加property变量，标识属性名
 * 自定义jsr303错误消息时，可以使用{property}变量
 */
public class CustomMessageInterpolator extends ResourceBundleMessageInterpolator {

    @Override
    protected String interpolate(Context context, Locale locale, String term) {
        return super.interpolate(new VarContext((MessageInterpolatorContext) context), locale, term);
    }

    @Override
    public String interpolate(String message, Context context) {
        return super.interpolate(message, new VarContext((MessageInterpolatorContext) context));
    }

    @Override
    public String interpolate(String message, Context context, Locale locale) {
        return super.interpolate(message, new VarContext((MessageInterpolatorContext) context), locale);
    }

    /**
     * 支持属性名插值的上下文对象
     */
    static class VarContext extends MessageInterpolatorContext {
        public VarContext(MessageInterpolatorContext context) {
            super(context.getConstraintDescriptor(), context.getValidatedValue(), context.getRootBeanType(), context.getPropertyPath(),
                    context.getMessageParameters(), context.getExpressionVariables(), context.getExpressionLanguageFeatureLevel(),
                    context.isCustomViolation());
        }
        public VarContext(ConstraintDescriptor<?> constraintDescriptor, Object validatedValue, Class<?> rootBeanType, Path propertyPath, Map<String, Object> messageParameters, Map<String, Object> expressionVariables, ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel, boolean customViolation) {
            super(constraintDescriptor, validatedValue, rootBeanType, propertyPath, messageParameters, expressionVariables, expressionLanguageFeatureLevel, customViolation);
        }

        @Override
        public Map<String, Object> getExpressionVariables() {
            HashMap<String, Object> map = new HashMap<>(super.getExpressionVariables());
            map.put("property", getPropertyPath().toString());
            return map;
        }

        @Override
        public Map<String, Object> getMessageParameters() {
            HashMap<String, Object> map = new HashMap<>(super.getMessageParameters());
            ArrayList<Path.Node> nodes = ListUtil.toList(getPropertyPath());
            map.put("property", nodes.get(nodes.size() - 1).toString());
            return map;
        }
    }
}
