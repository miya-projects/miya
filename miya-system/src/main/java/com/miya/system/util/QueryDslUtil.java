package com.miya.system.util;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

public class QueryDslUtil {


    /**
     * 通过order创建Expression。用于排序，针对特殊字段类型进行特殊处理，如MultiCurrencyMoney
     *
     * @param order must not be {@literal null}.
     */
    public static Expression<?> buildOrderPropertyPathFrom(Path<?> rootPath, Sort.Order order) {
        PathBuilder builder = new PathBuilder(rootPath.getType(), rootPath.getMetadata());
        Assert.notNull(order, "Order must not be null!");

        PropertyPath path = PropertyPath.from(order.getProperty(), builder.getType());
        Expression<?> sortPropertyExpression = builder;

        while (path != null) {

            sortPropertyExpression = !path.hasNext() && order.isIgnoreCase() && String.class.equals(path.getType()) //
                    ? Expressions.stringPath((Path<?>) sortPropertyExpression, path.getSegment()).lower() //
                    : Expressions.path(path.getType(), (Path<?>) sortPropertyExpression, path.getSegment());
            path = path.next();
        }

        // 针对MultiCurrencyMoney类型单独处理
        // if (sortPropertyExpression.getType().equals(MultiCurrencyMoney.class)){
        //     return new QMultiCurrencyMoney(((SimplePath<MultiCurrencyMoney>) sortPropertyExpression).getMetadata()).cnyAmount();
        // }
        return sortPropertyExpression;
    }


    /**
     * pageable转QPageRequest 用于特殊字段排序
     * @param rootPath
     * @param pageable
     */
    public static QPageRequest toQPageRequest(Path<?> rootPath, Pageable pageable){
        final List<OrderSpecifier> orderSpecifiers = pageable.getSort().stream()
                .map(order -> new OrderSpecifier(order.getDirection().isDescending()? Order.DESC:Order.ASC,
                        QueryDslUtil.buildOrderPropertyPathFrom(rootPath, order)))
                .collect(Collectors.toList());
        return QPageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), orderSpecifiers.toArray(new OrderSpecifier[0]));
    }
}
