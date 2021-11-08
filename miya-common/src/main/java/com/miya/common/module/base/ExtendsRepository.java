package com.miya.common.module.base;

import com.miya.common.model.dto.base.Grid;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 对repository扩展一些功能
 * @param <T>
 * @param <ID>
 */
public interface ExtendsRepository<T, ID, S extends EntityPath<?>> {

    /**
     * 支持返回一个指定的DTO
     * @param predicate
     * @param clazz DTO类对象
     * @param <D>   DTO类型
     * @return
     */
    @Deprecated
    <D> List<D> findAll(Predicate predicate, Class<D> clazz);

    /**
     * 支持指定DTO，直接返回Grid
     * @param predicate
     * @param pageable
     * @param clazz
     * @param <D>
     * @return
     */
    @Deprecated
    <D> Grid<D> findAll(Predicate predicate, Pageable pageable, Class<D> clazz);

}
