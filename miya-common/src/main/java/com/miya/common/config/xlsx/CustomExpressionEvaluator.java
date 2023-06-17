package com.miya.common.config.xlsx;

import lombok.NonNull;
import org.jxls.expression.ExpressionEvaluator;
import org.springframework.context.expression.BeanExpressionContextAccessor;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主要是适配ToExcelFormat
 */

public class CustomExpressionEvaluator implements ExpressionEvaluator {
    ExpressionParser parser = new SpelExpressionParser();

    private String defaultExpression;
    private List<PropertyAccessor> propertyAccessors;

    public CustomExpressionEvaluator() {
        propertyAccessors = new ArrayList<>();
        propertyAccessors.add(new MapAccessor());
        propertyAccessors.add(new BeanExpressionContextAccessor());
        propertyAccessors.add(new BeanFactoryAccessor());
        propertyAccessors.add(new EnvironmentAccessor());
        propertyAccessors.add(new ReflectivePropertyAccessor());
    }

    @Override
    public Object evaluate(String expression, Map<String, Object> context) {
        StandardEvaluationContext ctx = getStandardEvaluationContext(context);
        Object value = parser.parseExpression(expression).getValue(ctx);
        if (value instanceof ToExcelFormat toExcelFormat) {
            return toExcelFormat.toStringForExcel();
        }
        return value;
    }

    @Override
    public Object evaluate(Map<String, Object> context) {
        StandardEvaluationContext ctx = getStandardEvaluationContext(context);
        Object value = parser.parseExpression(defaultExpression).getValue(ctx);
        if (value instanceof ToExcelFormat) {
            return ((ToExcelFormat)value).toStringForExcel();
        }
        return value;
    }

    @NonNull
    private StandardEvaluationContext getStandardEvaluationContext(Map<String, Object> context) {
        StandardEvaluationContext ctx = new StandardEvaluationContext(context);
        ctx.setPropertyAccessors(propertyAccessors);
        return ctx;
    }


    @Override
    public String getExpression() {
        return defaultExpression;
    }
}
