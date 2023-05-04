package com.miya.common.module.base;

import cn.hutool.core.date.DateUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import lombok.NonNull;
import org.springframework.data.querydsl.binding.MultiValueBinding;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.util.CastUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 常见类型参数查询条件绑定器
 */
public class DefaultQuerydslBinder {

    public static void customize(QuerydslBindings bindings, Path<?> root) {

        final DateMultiValueBinding dateBinding = new DateMultiValueBinding();
        bindings.bind(Date.class)
                .all(dateBinding);
        bindings.bind(Timestamp.class)
                .all(CastUtils.cast(dateBinding));
        bindings.bind(LocalDate.class).all(new LocalDateMultiValueBinding());
        bindings.bind(LocalDateTime.class).all(new LocalDateTimeMultiValueBinding());
        bindings.bind(YearMonth.class).all(new YearMonthMultiValueBinding());

        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);

        bindings.excluding(QBaseEntity.baseEntity.createdUser);
    }

    static class DateMultiValueBinding implements MultiValueBinding<Path<Date>, Date> {

        @Override
        public @NonNull Optional<Predicate> bind(Path<Date> path, Collection<? extends Date> value) {
            if (value.size() < 1 || value.size() > 2) {
                throw new IllegalArgumentException("参数数量不匹配");
            }
            DateTimePath<Date> dateTimePath = CastUtils.cast(path);
            Iterator<? extends Date> iterator = value.iterator();
            if (value.size() == 1) {
                Date date = iterator.next();
                if (date == null){
                    return Optional.empty();
                }
                Date dateTimeStart = DateUtil.beginOfDay(date).toJdkDate();
                Date dateTimeEnd = DateUtil.endOfDay(date).toJdkDate();
                return Optional.of(dateTimePath.between(dateTimeStart, dateTimeEnd));
            }
            Date startTime = iterator.next();
            Date endTime = iterator.next();
            if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
                return Optional.empty();
            }
            return Optional.of(
                    dateTimePath.between(
                            DateUtil.beginOfDay(startTime).toJdkDate(),
                            DateUtil.endOfDay(endTime).toJdkDate()
                    )
            );
        }
    }

    static class LocalDateMultiValueBinding implements MultiValueBinding<Path<LocalDate>, LocalDate> {

        @Override
        public @NonNull Optional<Predicate> bind(Path<LocalDate> path, Collection<? extends LocalDate> value) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            DatePath<LocalDate> localDateDatePath = CastUtils.cast(path);
            final List<? extends LocalDate> localDates = new ArrayList<>(value);
            // 精确打击
            if (localDates.size() == 1) {
                LocalDate localDate = localDates.get(0);
                booleanBuilder.and(localDateDatePath.eq(localDate));
                return Optional.of(booleanBuilder);
            }
            // 范围攻击
            if (localDates.size() == 2) {
                Collections.sort(localDates);
                LocalDate startDate = localDates.get(0);
                LocalDate endDate = localDates.get(1);
                if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
                    return Optional.empty();
                }
                booleanBuilder.and(localDateDatePath.between(startDate, endDate));
                return Optional.of(booleanBuilder);
            }

            throw new IllegalArgumentException("LocalDate 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");
        }
    }

    static class LocalDateTimeMultiValueBinding implements MultiValueBinding<Path<LocalDateTime>, LocalDateTime> {

        @Override
        public @NonNull Optional<Predicate> bind(Path<LocalDateTime> path, Collection<? extends LocalDateTime> value) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            DatePath<LocalDateTime> localDateTimeDatePath = CastUtils.cast(path);
            final List<? extends LocalDateTime> localDates = new ArrayList<>(value);
            // 精确打击
            if (localDates.size() == 1) {
                LocalDateTime localDateTime = localDates.get(0);
                booleanBuilder.and(localDateTimeDatePath.eq(localDateTime));
                return Optional.of(booleanBuilder);
            }
            // 范围攻击
            if (localDates.size() == 2) {
                Collections.sort(localDates);
                LocalDateTime startDateTime = localDates.get(0);
                LocalDateTime endDateTime = localDates.get(1);
                if (Objects.isNull(startDateTime) || Objects.isNull(endDateTime)) {
                    return Optional.empty();
                }
                booleanBuilder.and(localDateTimeDatePath.between(startDateTime, endDateTime));
                return Optional.of(booleanBuilder);
            }

            throw new IllegalArgumentException("LocalDateTime 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");
        }
    }

    static class YearMonthMultiValueBinding implements MultiValueBinding<Path<YearMonth>, YearMonth> {

        @Override
        public @NonNull Optional<Predicate> bind(Path<YearMonth> path, Collection<? extends YearMonth> value) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            DatePath<YearMonth> yearMonthPath = CastUtils.cast(path);
            final List<? extends YearMonth> yearMonths = new ArrayList<>(value);
            // 精确打击
            if (yearMonths.size() == 1) {
                YearMonth localDateTime = yearMonths.get(0);
                booleanBuilder.and(yearMonthPath.eq(localDateTime));
                return Optional.of(booleanBuilder);
            }
            // 范围攻击
            if (yearMonths.size() == 2) {
                Collections.sort(yearMonths);
                YearMonth startYearMonth = yearMonths.get(0);
                YearMonth endYearMonth = yearMonths.get(1);
                if (Objects.isNull(startYearMonth) || Objects.isNull(endYearMonth)) {
                    return Optional.empty();
                }
                booleanBuilder.and(yearMonthPath.between(startYearMonth, endYearMonth));
                return Optional.of(booleanBuilder);
            }

            throw new IllegalArgumentException("YearMonth 参数个数不匹配！要么是单个(精确)，要么是两个(范围)!");
        }
    }

}
