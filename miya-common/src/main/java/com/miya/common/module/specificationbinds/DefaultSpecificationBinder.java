package com.miya.common.module.specificationbinds;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmPluralValuedSimplePath;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.CastUtils;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 默认的specification绑定器
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSpecificationBinder implements SpecificationBinder<Path<?>>, SmartInitializingSingleton {

    public static DefaultSpecificationBinder INSTANCE;

    private final ConversionService conversionService;


    @Override
    public void afterSingletonsInstantiated() {
        INSTANCE = SpringUtil.getBean(DefaultSpecificationBinder.class);
    }

    @Nullable
    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<?> path, @Nullable String[] values) {
        if (values == null || values.length == 0) {
            return cb.isNull(path);
        }
        if (path instanceof SqmBasicValuedSimplePath<?> simplePath) {
            if (values.length == 1) {
                return cb.equal(path, values[0]);
            }
            CriteriaBuilder.In<String> in = CastUtils.cast(cb.in(simplePath));
            for (String value : values) {
                in.value(value);
            }
            return in;
        }
        if (path instanceof SqmPluralValuedSimplePath<?> pluralPath) {
            // 搜索多个角色，多对多
            //CriteriaBuilder.In<String> in = CastUtils.cast(cb.in(pluralPath));
            List<Predicate> list = Arrays.stream(values).map(val -> {
                if (conversionService.canConvert(String.class, pluralPath.getJavaType())) {
                    Object convert = conversionService.convert(val, pluralPath.getJavaType());
                    return cb.and(cb.isMember(convert, CastUtils.cast(pluralPath)));
                }
                return null;
            }).toList();
            return cb.and(list.toArray(new Predicate[0]));
        }
        log.error("未知的path");
        return null;
    }

}
