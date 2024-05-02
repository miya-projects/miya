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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 主要是适配ToExcelFormat
 */

public class CustomExpressionEvaluator implements ExpressionEvaluator {
    ExpressionParser parser = new SpelExpressionParser();

    private String defaultExpression;
    private List<PropertyAccessor> propertyAccessors;

    public CustomExpressionEvaluator(String expression) {
        this.defaultExpression = expression;
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
        return wrapValue(value);
    }

    @Override
    public Object evaluate(Map<String, Object> context) {
        StandardEvaluationContext ctx = getStandardEvaluationContext(context);
        Object value = parser.parseExpression(defaultExpression).getValue(ctx);
        return wrapValue(value);
    }

    /**
     * 将表达式解析结果再进行处理，支持ToExcelFormat和集合的情况
     * @param value 解析结果
     * @return  处理后的值
     */
    private static Object wrapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof ToExcelFormat) {
            return ((ToExcelFormat)value).toStringForExcel();
        }
        if (value instanceof Collection<?> collection) {
            ArrayList<Object> objects = new ExcelFormatArrayList();
            objects.addAll(collection);
            return objects;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            ArrayList<Object> objects = new ExcelFormatArrayList();
            List<Object> list = IntStream.range(0, length)
                    .mapToObj(i -> Array.get(value, i))
                    .toList();
            objects.addAll(list);
            return objects;
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


    private static class ExcelFormatArrayList extends ArrayList<Object> {
        @Override
        public String toString() {
            String str = this.stream().map(item -> {
                if (item instanceof ToExcelFormat) {
                    return ((ToExcelFormat)item).toStringForExcel();
                }
                if (item == null) {
                    return "";
                }
                return item.toString();
            }).collect(Collectors.joining(","));
            return "[" + str + "]";
        }
    }
}
