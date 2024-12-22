package com.miya.common.module.specificationbinds.binds;

import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.lang.Nullable;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YearMonthBinding implements SpecificationBinder<Path<YearMonth>> {

    @Nullable
    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<YearMonth> path, String[] values) {
        if (values.length == 1) {
            if (values[0] == null) {
                return null;
            }
            YearMonth yearMonth = YearMonth.parse(values[0]);
            return cb.equal(path, yearMonth);
        }
        if (values.length == 2) {
            List<String> list = Arrays.asList(values);
            Collections.sort(list);
            YearMonth startYearMonth = YearMonth.parse(values[0]);
            YearMonth endYearMonth = YearMonth.parse(values[1]);
            return cb.between(path, startYearMonth, endYearMonth);
        }
        throw new IllegalArgumentException("YearMonth 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");

    }
}
