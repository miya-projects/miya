package com.miya.common.module.specificationbinds.binds;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LocalDateTimeBinding implements SpecificationBinder<Path<LocalDateTime>> {

    @Nullable
    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<LocalDateTime> path, String[] values) {
        if (values.length == 1) {
            LocalDateTime date = LocalDateTimeUtil.parse(values[0]);
            if (date == null) {
                return null;
            }
            return cb.equal(path, date);
        }
        if (values.length == 2) {
            List<String> list = Arrays.asList(values);
            Collections.sort(list);
            LocalDateTime startTime = LocalDateTimeUtil.parse(list.get(0));
            LocalDateTime endTime = LocalDateTimeUtil.parse(list.get(1));
            if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
                return null;
            }
            return cb.between(path, startTime, endTime);
        }
        throw new IllegalArgumentException("LocalDateTime 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");
    }

}
