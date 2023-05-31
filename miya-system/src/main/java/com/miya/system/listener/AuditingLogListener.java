package com.miya.system.listener;


import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.system.module.log.LogService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Entity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 数据变动Log监听
 * todo 同一个对象变动多次会触发多次
 */
@Slf4j
public class AuditingLogListener implements Interceptor {

    private final String LOG_TYPE = "实体数据变化";

    private LogService logService;


    @SneakyThrows({IllegalArgumentException.class, IllegalAccessException.class})
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PostPersist
    public void postPersist(Object entity) {
        Class<?> aClass = entity.getClass();
        String summary = StrUtil.format("新增了{}表数据", aClass.getName());
        Field[] declaredFields = ClassUtil.getDeclaredFields(entity.getClass());
        Map<String, Object> map = new HashMap<>();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Object value = declaredField.get(entity);
            if (isEntity(declaredField.getType())) {
                Serializable id = (Serializable) ReflectUtil.getFieldValue(value, "id");
                map.put(declaredField.getName(), id);
            } else {
                map.put(declaredField.getName(), declaredField.get(entity));
            }
        }
        getLogService().log(LOG_TYPE, summary, "新增", null, map);
    }

    @SneakyThrows({IllegalArgumentException.class, IllegalAccessException.class})
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) throws CallbackException {
        String summary = StrUtil.format("更新了{}表数据", entity.getClass().getName());
        Map<String, Object> oldEntity = new HashMap<>();
        Map<String, Object> newEntity = new HashMap<>();
        for (int i = 0; i < propertyNames.length; i++) {
            boolean flag = Objects.equals(previousState[i], currentState[i]);
            if (flag) {
                continue;
            }
            if (Objects.nonNull(previousState[i]) && isEntity(previousState[i].getClass())) {
                Field idField = ReflectUtil.getField(previousState[i].getClass(), "id");
                if (Objects.isNull(idField)) {
                    continue;
                }
                idField.setAccessible(true);
                Serializable oldIdValue = (Serializable) idField.get(previousState[i]);
                if (Objects.nonNull(oldIdValue) && isEntity(oldIdValue.getClass())) {
                    oldIdValue = (Serializable) ReflectUtil.getFieldValue(oldIdValue, "id");
                }
                oldEntity.put(propertyNames[i], oldIdValue);
            } else {
                oldEntity.put(propertyNames[i], previousState[i]);
            }

            if (Objects.nonNull(currentState[i]) && isEntity(currentState[i].getClass())) {
                Field idField = ReflectUtil.getField(currentState[i].getClass(), "id");
                if (Objects.isNull(idField)) {
                    continue;
                }
                idField.setAccessible(true);
                Serializable newIdValue = (Serializable) idField.get(currentState[i]);
                if (Objects.nonNull(newIdValue) && isEntity(newIdValue.getClass())) {
                    newIdValue = (Serializable) ReflectUtil.getFieldValue(newIdValue, "id");
                }
                newEntity.put(propertyNames[i], newIdValue);
            } else {
                newEntity.put(propertyNames[i], currentState[i]);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("oldEntity", oldEntity);
        map.put("newEntity", newEntity);
        map.put("id", id);
        getLogService().log(LOG_TYPE, summary, "更新", id.toString(), map);
        return false;
    }

    @PostRemove
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postRemove(Object entity) {
        Class<?> aClass = entity.getClass();
        String summary = StrUtil.format("删除了{}表数据", aClass.getName());
        getLogService().log(LOG_TYPE, summary, "删除", ReflectUtil.getFieldValue(entity, "id").toString(), null);
    }

    /**
     * 判断一个类是否是实体类
     * @param clazz
     * @return
     */
    private boolean isEntity(Class<?> clazz) {
        return Objects.nonNull(clazz.getAnnotation(Entity.class));
    }

    private LogService getLogService() {
        if (logService == null) {
            logService = SpringUtil.getBean(LogService.class);
        }
        return logService;
    }

}
