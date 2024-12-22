package com.miya.common.module.specificationbinds;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

public class EntitySpecification<T> implements Specification<T> {

    private final EntityBinder entityBinder;

    private final Map<String, String[]> parameterMap;

    public EntitySpecification(EntityBinder entityBinder, Map<String, String[]> parameterMap) {
        this.entityBinder = entityBinder;
        this.parameterMap = parameterMap;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return entityBinder.bind(root, query, criteriaBuilder, parameterMap);
    }
}
