package com.miya.common.module.specificationbinds.binds;

import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.*;

public class StringBinding implements SpecificationBinder<Path<String>> {

    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<String> path, String[] values) {
        return cb.like(path, "%" + values[0] + "%");
    }
}
