package com.miya.common.module.specificationbinds;

/**
 * 自定义各个实体的构造Specification对象的字段绑定方式
 * todo
 * @param <T>
 */
public interface SpecificationBinderCustomizer<T> {

    void customize(EntityBinder entityBinder, T root);

}
