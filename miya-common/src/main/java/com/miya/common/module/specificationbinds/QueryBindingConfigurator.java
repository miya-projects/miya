package com.miya.common.module.specificationbinds;


public interface QueryBindingConfigurator {

    /**
     * @param entityClass 实体类类型
     * @return 字段绑定配置
     */
    EntityBinder getBindingsForEntity(Class<?> entityClass);
}
