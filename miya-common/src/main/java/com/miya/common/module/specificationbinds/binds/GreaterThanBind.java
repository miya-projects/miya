package com.miya.common.module.specificationbinds.binds;

import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public class GreaterThanBind implements SpecificationBinder<Path<Integer>> {

    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<Integer> path, String[] values) {
        return cb.greaterThan(path, Integer.parseInt(values[0]));
    }

}
