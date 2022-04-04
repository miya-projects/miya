package com.miya.system.config.orm;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import ext.java.util.QMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 扩展Querydsl内置类型
 * 只对当前模块生效
 */
public class QueryDslExtensions {

    @QueryDelegate(String.class)
    public static StringTemplate groupConcat(SimpleExpression<String> expression) {
        return Expressions.stringTemplate("group_concat({0})", expression);
    }

    @QueryDelegate(String.class)
    public static StringTemplate groupConcat(SimpleExpression<String> expression, String separator) {
        return Expressions.stringTemplate("group_concat({0} SEPARATOR '" + separator + "' )", expression);
    }

    @QueryDelegate(LocalDate.class)
    public static StringTemplate dateFormat(SimpleExpression<LocalDate> expression, String format) {
        return Expressions.stringTemplate("date_format({0}, {1})", expression, format);
    }

    @QueryDelegate(Map.class)
    public static StringTemplate jsonExtractStr(QMap expression, String jsonField) {
        return Expressions.stringTemplate("json_extract({0}, '" + jsonField + "' )", expression);
    }

    @QueryDelegate(Map.class)
    public static NumberExpression<BigDecimal> jsonExtractNum(QMap expression, String jsonField) {
        return Expressions.stringTemplate("json_extract({0}, '" + jsonField + "' )", expression).castToNum(BigDecimal.class);
    }

}
