package com.miya.common.module.specificationbinds.binds;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.*;

public class LocalDateBinding implements SpecificationBinder<Path<LocalDate>> {

    @Nullable
    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<LocalDate> path, String[] values) {
        if (values.length == 1) {
            LocalDate date = LocalDateTimeUtil.parseDate(values[0]);
            if (date == null) {
                return null;
            }
            return cb.equal(path, date);
        }
        if (values.length == 2) {
            List<String> list = Arrays.asList(values);
            Collections.sort(list);
            LocalDate startTime = LocalDateTimeUtil.parseDate(list.get(0));
            LocalDate endTime = LocalDateTimeUtil.parseDate(list.get(1));
            if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
                return null;
            }
            return cb.between(path, startTime, endTime);
        }
        throw new IllegalArgumentException("LocalDateTime 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");
    }
}
