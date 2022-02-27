package com.miya.common.annotation.constraint;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 自定义消息变量插值器
 */
public class CustomMessageInterpolator extends ResourceBundleMessageInterpolator {

    @Override
    protected String interpolate(Context context, Locale locale, String term) {
        return super.interpolate(new VarContext((MessageInterpolatorContext) context), locale, term);
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
    }
}
