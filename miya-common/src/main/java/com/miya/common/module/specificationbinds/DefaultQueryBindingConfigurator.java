package com.miya.common.module.specificationbinds;

import cn.hutool.core.util.TypeUtil;
import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.specificationbinds.binds.*;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.util.CastUtils;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * 默认的查询参数绑定器
 * 用于获取binder对象和配置自定义数据和自定义bind方式
 */
@RequiredArgsConstructor
public class DefaultQueryBindingConfigurator implements QueryBindingConfigurator, InitializingBean {

    private final Map<Class<?>, EntityBinder> configuration = new HashMap<>();
    private final Map<Class<?>, SpecificationBinder<? extends Path<?>>> defaultBinderMap = new HashMap<>();

    {
        defaultBinderMap.put(String.class, new StringBinding());
        defaultBinderMap.put(Date.class, new DateBinding());
        defaultBinderMap.put(LocalDate.class, new LocalDateBinding());
        defaultBinderMap.put(LocalDateTime.class, new LocalDateTimeBinding());
        defaultBinderMap.put(YearMonth.class, new YearMonthBinding());
        //defaultBinderMap.put(Timestamp.class, new DateBinding());
    }

    private final List<SpecificationBinderCustomizer<?>> specificationBinderCustomizers;

    @Override
    public EntityBinder getBindingsForEntity(Class<?> entityClass) {
        return configuration.computeIfAbsent(entityClass, this::generateDefaultBindings);
    }

    // 根据字段类型生成默认的绑定方式
    private EntityBinder generateDefaultBindings(Class<?> entityClass) {
        Map<String, SpecificationBinder<? extends Path<?>>> bindings = new HashMap<>();
        for (var field : entityClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            //fieldType.isEnum()
            // 其他实体
            // 自定义类，json?
            //Enum.class.isAssignableFrom(fieldType); true
            SpecificationBinder<? extends Path<?>> specificationBinder = defaultBinderMap.get(fieldType);
            if (specificationBinder == null) {
                continue;
            }
            bindings.put(field.getName(), specificationBinder);
        }
        return new EntityBinder(bindings);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // todo
        for (SpecificationBinderCustomizer<?> customizer : specificationBinderCustomizers) {
            Class<? extends SpecificationBinderCustomizer> aClass = customizer.getClass();
            Type typeArgument = TypeUtil.getTypeArgument(aClass);
            if (typeArgument == null) {
                continue;
            }
            EntityBinder entityBinder = getBindingsForEntity((Class<?>)typeArgument);
            customizer.customize(entityBinder, CastUtils.cast(typeArgument));
        }
    }


    // 允许手动配置或覆盖字段的查询绑定方式
    //public void setBindingForField(Class<?> entityClass, String fieldName, SpecificationBinder<?> specificationBinder) {
    //    configuration.computeIfAbsent(entityClass, k -> new HashMap<>()).put(fieldName, specificationBinder);
    //}


}
