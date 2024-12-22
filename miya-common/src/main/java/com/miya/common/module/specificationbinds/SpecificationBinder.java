package com.miya.common.module.specificationbinds;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.formula.functions.T;

@FunctionalInterface
public interface SpecificationBinder<T extends Path<?>> {

    /**
     * 生成对应的查询条件
     *
     * @param cb JPA 的 CriteriaBuilder
     * @param path 字段路径
     * @param values 查询的值
     * @return 对应的查询条件
     */
    @Nullable
    Predicate buildPredicate(CriteriaBuilder cb, T path, @Nullable String[] values);
}
