package com.miya.common.module.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.annotation.FieldMapping;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.util.CastUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 前端Form基类
 */
@Slf4j
public abstract class BaseForm<PO> {

    private JPAQueryFactory getJpaQueryFactory() {
        return SpringUtil.getBean(JPAQueryFactory.class);
    }

    /**
     * 转换为PO 用于持久化
     *
     * @return PO
     */
    @SneakyThrows
    public PO mergeToNewPo() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class<PO> entityClass = CastUtils.cast(params[0]);
        PO t = entityClass.newInstance();
        mergeToPo(t);
        return t;
    }


    /**
     * 转换为po 用于持久化
     * 和toPO()的区别是，Form直接转换为po后，还需要将非空字段copy
     * 到已持久化的po(如果复制空字段将会导致dto中没有的字段都为空字段)，这时，如果真正想要置null的字段将不会被修改
     * 而此方法会直接将dto有的字段复制到已持久化的po，实现null值的修改，省去po复制到po的过程
     *
     * @param t 已持久化的po
     */
    @SneakyThrows
    public void mergeToPo(PO t) {
        /**
         * 获取子类泛型class
         */
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Class<PO> entityClass = CastUtils.cast(params[0]);
        HashMap<String, String> mapping = new HashMap<>();
        Field[] declaredFields = this.getClass().getDeclaredFields();
        // form字段
        List<Field> fields = Stream.of(declaredFields)
                .filter(field -> Objects.nonNull(field.getAnnotation(FieldMapping.class))).collect(Collectors.toList());
        List<String> ignoreProperties = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            //获取元信息
            FieldMapping annotation = field.getAnnotation(FieldMapping.class);
            String mappedFieldName = field.getName();
            Class<?> mappingClass = annotation.mappingClass();
            if (StringUtils.isNotBlank(annotation.value())) {
                mappedFieldName = annotation.value();
                mapping.put(field.getName(), mappedFieldName);
            }
            if (!mappingClass.equals(Void.class)) {
                //构造外键映射的目标对象o 或者Collection<o>
                Object o = null;
                SimpleEntityPathResolver simpleEntityPathResolver = SimpleEntityPathResolver.INSTANCE;
                EntityPath<?> path = simpleEntityPathResolver.createPath(mappingClass);
                BeanPath<?> idPath = new BeanPath<>(mappingClass, "id");
                if (field.get(this) instanceof Collection) {
                    //处理集合的情况
                    Collection<Object> c = CastUtils.cast(field.get(this));
                    Collection<Object> newCollection = CastUtils.cast(field.get(this).getClass().newInstance());
                    for (Object i : c) {
                        JPAQuery<?> newQuery = getJpaQueryFactory().select(path)
                                .from(path).where(Expressions.booleanOperation(Ops.EQ, idPath, ConstantImpl.create(i)));
                        newCollection.add(newQuery.fetchOne());
                    }
                    // entity field
                    Field mappedField = ReflectUtil.getField(entityClass, mappedFieldName);
                    mappedField.setAccessible(true);
                    final Collection<Object> o1 = CollUtil.create(mappedField.getType());
                    o1.addAll(newCollection);
                    o = o1;
                } else {
                    Object o1 = field.get(this);
                    if (o1 != null) {
                        JPAQuery<?> newQuery = getJpaQueryFactory().select(path)
                                .from(path).where(Expressions.booleanOperation(Ops.EQ, idPath, ConstantImpl.create(o1)));
                        o = newQuery.fetchOne();
                    }
                }
                //不拷贝当前对象，因为类型不一致，而且已经设置值了
                ignoreProperties.add(mappedFieldName);
                //为目标对象设置外键对象
                Field mappedField = ReflectUtil.getField(entityClass, mappedFieldName);
                if (mappedField == null){
                    throw new RuntimeException(StrUtil.format("实体类【{}】不存在与dto【{}】对应的属性【{}】,",
                            mappingClass.getSimpleName(), this.getClass().getSimpleName(), mappedFieldName));
                }
                mappedField.setAccessible(true);
                try {
                    mappedField.set(t, o);
                } catch (IllegalArgumentException e) {
                    log.error("请检查dto内配置mappingClass是否和目标类对应: {}", e.getMessage());
                    throw e;
                }
            }
        }
        CopyOptions copyOptions = CopyOptions.create().setFieldMapping(mapping)
                .setIgnoreProperties(ignoreProperties.toArray(new String[0]));
        BeanUtil.copyProperties(this, t, copyOptions);
    }


}
