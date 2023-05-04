package com.miya.common.module.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.CastUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 前端Form基类
 */
@Slf4j
public abstract class BaseForm<PO> {

    private JPAQueryFactory getJpaQueryFactory() {
        return SpringUtil.getBean(JPAQueryFactory.class);
    }

    /**
     * 将form数据合并到新=的po中，如果类型不一致，使用spring类型转换系统转换
     * @return PO
     */
    @SneakyThrows({IllegalAccessException.class, InstantiationException.class})
    public PO mergeToNewPo() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class<PO> entityClass = CastUtils.cast(params[0]);
        PO t = entityClass.newInstance();
        mergeToPo(t);
        return t;
    }


    /**
     * 将form数据合并到指定的po中，如果类型不一致，使用spring类型转换系统转换
     * @param t 已持久化的po
     */
    public void mergeToPo(PO t) {
        // * 获取子类泛型class
        Class<PO> entityClass = CastUtils.cast(ClassUtil.getTypeArgument(getClass()));

        // form中的字段
        Field[] declaredFieldsForForm = this.getClass().getDeclaredFields();
        // entity中的字段
        Field[] declaredFieldsForEntity = entityClass.getDeclaredFields();

        List<String> fieldNamesForEntity = Arrays.stream(declaredFieldsForEntity).map(Field::getName).collect(Collectors.toList());

        // form中entity没有的字段，这些字段应当有form自己特殊处理
        // List<Field> detachFields = Arrays.stream(declaredFieldsForForm).filter(field -> !fieldNamesForEntity.contains(field.getName())).collect(Collectors.toList());
        // if (detachFields.size() > 0) {
        //     throw new RuntimeException(StrUtil.format("字段【{}】在class 【{}】中不存在",
        //             detachFields.stream().map(Field::getName).collect(Collectors.toList()), entityClass.getSimpleName()));
        // }
        ConversionService conversionService = SpringUtil.getBean(ConversionService.class);


        // 在form中和entity类型不同的字段 使用spring类型转换系统转换
        List<Field> fieldsInFormDiffType = Arrays.stream(declaredFieldsForForm)
                .filter(field -> {
                    Field fieldInEntity = ReflectUtil.getField(entityClass, field.getName());
                    if (fieldInEntity == null) {
                        return false;
                    }
                    return !field.getType().equals(fieldInEntity.getType());
                })
                .collect(Collectors.toList());

        List<String> ignoreProperties = new ArrayList<>();
        for (Field field : fieldsInFormDiffType) {
            Object o = conversionService.convert(ReflectUtil.getFieldValue(this, field), ReflectUtil.getField(entityClass, field.getName()).getType());
            ReflectUtil.setFieldValue(t, field.getName(), o);
            ignoreProperties.add(field.getName());
        }

        // 在form中集合类型字段
        List<Field> fieldsInFormCollType = Arrays.stream(declaredFieldsForForm)
                .filter(field -> Collection.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        for (Field field : fieldsInFormCollType) {
            Collection<Object> collection = CollUtil.create(field.getType());
            Collection<?> collInForm = (Collection<?>)ReflectUtil.getFieldValue(this, field);
            if (collInForm == null){
                collInForm = CollUtil.create(field.getType());
            }
            // 集合元素的类型
            Class<?> itemClass = TypeUtil.getClass(TypeUtil.getTypeArgument(ReflectUtil.getField(entityClass, field.getName()).getGenericType()));

            for (Object item : collInForm) {
                collection.add(conversionService.convert(item, itemClass));
            }

            ReflectUtil.setFieldValue(t, field.getName(), collection);
            ignoreProperties.add(field.getName());
        }


        CopyOptions copyOptions = CopyOptions.create()
                .setIgnoreProperties(ignoreProperties.toArray(new String[0]));
        BeanUtil.copyProperties(this, t, copyOptions);
    }

}
