package com.miya.system.config.web;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.miya.common.config.web.databind.ControllerAdviceInitBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
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

        extendsDateLike(builder);

    }

    /**
     * 扩展日期类型序列化方式
     * @param builder
     */
    private static void extendsDateLike(Jackson2ObjectMapperBuilder builder) {
        builder.deserializerByType(Date.class, new StdScalarDeserializer<Date>(Date.class){
            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                String value = p.getValueAsString();
                try {
                    return new Date(Long.parseLong(value));
                } catch (NumberFormatException e) {
                    return Convert.toDate(value);
                }
            }
        });
        builder.deserializerByType(LocalDate.class, new StdScalarDeserializer<LocalDate>(LocalDate.class){
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                LocalDateTime localDateTime = Convert.toLocalDateTime(p.getValueAsString());
                if (localDateTime == null){
                    return null;
                }
                return LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
            }
        });

        builder.deserializerByType(LocalDateTime.class, new StdScalarDeserializer<LocalDateTime>(LocalDateTime.class){
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                return Convert.toLocalDateTime(p.getValueAsString());
            }
        });
        builder.deserializerByType(Timestamp.class, new StdScalarDeserializer<Timestamp>(Timestamp.class){
            @Override
            public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                return Optional.ofNullable(Convert.toLocalDateTime(p.getValueAsString())).map(Timestamp::valueOf).orElse(null);
            }
        });
    }


    /**
     * 1. trim
     * 2。去不可见字符
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
