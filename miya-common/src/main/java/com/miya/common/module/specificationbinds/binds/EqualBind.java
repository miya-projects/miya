package com.miya.common.module.specificationbinds.binds;


import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public class EqualBind implements SpecificationBinder<Path<?>> {

    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<?> path, String[] values) {
        return cb.equal(path, values);
    }
}
