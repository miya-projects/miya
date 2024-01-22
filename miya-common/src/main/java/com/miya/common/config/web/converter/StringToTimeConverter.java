package com.miya.common.config.web.converter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * String转各种时间类型的转换器
 */
public class StringToTimeConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {

        return CollUtil.newHashSet(
                new ConvertiblePair(String.class, Date.class),
                new ConvertiblePair(String.class, LocalDateTime.class),
                new ConvertiblePair(String.class, LocalDate.class),
                new ConvertiblePair(String.class, LocalTime.class),
                new ConvertiblePair(String.class, YearMonth.class)
        );
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Class<?> type = targetType.getType();
        if (type.equals(Date.class)){
            return Convert.toDate(source);
        }
        if (type.equals(LocalDate.class)){
            LocalDateTime localDateTime = Convert.toLocalDateTime(source);
            if (localDateTime == null){
                return null;
            }
            return LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
        }
        if (type.equals(LocalDateTime.class)){
            return Convert.toLocalDateTime(source);
        }
        if (type.equals(Timestamp.class)){
            return Optional.ofNullable(Convert.toLocalDateTime(source)).map(Timestamp::valueOf).orElse(null);
        }
        if (type.equals(YearMonth.class)){
            return YearMonth.parse(source.toString());
        }
        if (type.equals(LocalTime.class)){
            return LocalTime.parse(source.toString());
        }
        return null;
    }
}
