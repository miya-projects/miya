package com.miya.common.module.specificationbinds.binds;

import cn.hutool.core.date.DateUtil;
import com.miya.common.module.specificationbinds.SpecificationBinder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Date;
import java.util.Objects;


public class DateBinding implements SpecificationBinder<Path<Date>> {

    @Override
    public Predicate buildPredicate(CriteriaBuilder cb, Path<Date> path, String[] values) {
        if (values.length == 1) {
            Date date = DateUtil.parse(values[0]);
            if (date == null){
                return null;
            }
            Date dateTimeStart = DateUtil.beginOfDay(date).toJdkDate();
            Date dateTimeEnd = DateUtil.endOfDay(date).toJdkDate();
            return cb.between(path, dateTimeStart, dateTimeEnd);
        }
        if (values.length == 2) {
            Date startTime = DateUtil.parse(values[0]);
            Date endTime = DateUtil.parse(values[1]);
            if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
                return null;
            }
            return cb.between(path, DateUtil.beginOfDay(startTime).toJdkDate(), DateUtil.endOfDay(endTime).toJdkDate());
        }
        throw new IllegalArgumentException("Date 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");
    }

}
