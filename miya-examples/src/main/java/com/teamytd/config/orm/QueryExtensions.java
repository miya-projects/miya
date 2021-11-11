package com.teamytd.config.orm;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringTemplate;

/**
 * 扩展Querydsl内置类型
 * 只对当前模块生效
 */
public class QueryExtensions {

    @QueryDelegate(String.class)
    public static StringTemplate groupConcat(SimpleExpression<String> expression) {
        return Expressions.stringTemplate("group_concat({0})", expression);
    }

    @QueryDelegate(String.class)
    public static StringTemplate groupConcat(SimpleExpression<String> expression, String separator) {
        return Expressions.stringTemplate("group_concat({0} SEPARATOR '" + separator + "' )", expression);
    }

}
