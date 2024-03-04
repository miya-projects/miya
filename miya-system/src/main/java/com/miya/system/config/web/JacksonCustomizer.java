package com.miya.system.config.web;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.miya.common.config.web.databind.ControllerAdviceInitBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * 定制jackson，只支持常用的日期类型，以及string类型的处理
 */
@Slf4j
@Component
public class JacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {

        builder.simpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);

        builder.timeZone(TimeZone.getDefault());
        builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        builder.serializerByType(ReadableEnum.class, new ReadableEnumSerializer());
        builder.deserializerByType(String.class, new StringTrimmerDeserializer(String.class));

        builder.postConfigurer(objectMapper -> {
            objectMapper.getFactory()
                    .setStreamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(500).build());
        });

        extendsDateLike(builder);

    }


    /**
     * 扩展日期类型序列化方式
     */
    private static void extendsDateLike(Jackson2ObjectMapperBuilder builder) {

        final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
        final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN);
        final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);

        // 序列化
        builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(localDateTimeFormatter))
                .serializerByType(LocalDate.class, new LocalDateSerializer(localDateFormatter))
                .serializerByType(LocalTime.class, new LocalTimeSerializer(localTimeFormatter))
                .serializerByType(Date.class, new DateSerializer(false, simpleDateFormat));

        // 反序列化
        builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(localDateTimeFormatter))
                .deserializerByType(LocalDate.class, new LocalDateDeserializer(localDateFormatter))
                .deserializerByType(LocalTime.class, new LocalTimeDeserializer(localTimeFormatter))
                .deserializerByType(Date.class,
                        new DateDeserializers.DateDeserializer(DateDeserializers.DateDeserializer.instance,
                                simpleDateFormat, DatePattern.NORM_TIME_PATTERN));
    }


    /**
     * 1. trim
     * 2. 去不可见字符
     */
    static class StringTrimmerDeserializer extends StdScalarDeserializer<String> {

        protected StringTrimmerDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            value = StringUtils.deleteAny(value, ControllerAdviceInitBinder.UN_AVAILABLE_STRING);
            return value.trim();
        }
    }

    /**
     * 定制序列化ReadableEnum
     */
    static class ReadableEnumSerializer extends JsonSerializer<ReadableEnum> {

        @Override
        public void serialize(ReadableEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (!(value instanceof Enum)){
                gen.writeString(value.getName());
                log.warn("ReadableEnum应用在了非Enum类型上");
                return;
            }
            String v = ((Enum<?>) value).name();
            gen.writeStartObject();
            gen.writeStringField("label", value.getName());
            gen.writeStringField("value", v);
            gen.writeEndObject();
        }
    }
}
