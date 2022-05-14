package com.miya.common.util;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 使jackson更易用
 */
public class JSONUtils {

    private static ObjectMapper objectMapper;

    /**
     * 构建objectMapper
     */
    private static final Supplier<ObjectMapper> objectMapperSupplier = () -> Jackson2ObjectMapperBuilder.json()
            .simpleDateFormat("yyyy-MM-dd HH:mm")
            .build();

    public static ObjectMapper getObjectMapper() {
        if (objectMapper != null) {
            return objectMapper;
        }
        // if (SpringUtil.getApplicationContext() == null) {
        //     return objectMapper = objectMapperSupplier.get();
        // }
        // ObjectMapper bean = SpringUtil.getBean(ObjectMapper.class);
        // return objectMapper = Optional.ofNullable(bean).orElseGet(objectMapperSupplier);

        return objectMapper = objectMapperSupplier.get();

    }

    /**
     * 将对象序列化
     *
     * @param obj
     */
    public static String toJson(Object obj) {
        try {
            return getObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反序列化对象字符串
     *
     * @param json
     * @param clazz
     */
    public static <T> T toJavaObject(String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反序列化字符串成为对象
     *
     * @param json
     * @param valueTypeRef
     */
    public static <T> T toJavaObject(String json, TypeReference<T> valueTypeRef) {
        try {
            return getObjectMapper().readValue(json, valueTypeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
