package com.miya.common.config.xlsx;

import lombok.extern.slf4j.Slf4j;
import org.jxls.expression.ExpressionEvaluator;
import org.jxls.expression.ExpressionEvaluatorFactory;

@Slf4j
public class CustomExpressionEvaluatorFactory implements ExpressionEvaluatorFactory {
    @Override
    public ExpressionEvaluator createExpressionEvaluator(String expression) {
        return new CustomExpressionEvaluator(expression);
    }
}
