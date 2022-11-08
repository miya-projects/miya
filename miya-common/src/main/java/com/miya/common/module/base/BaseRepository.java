package com.miya.common.module.base;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;


/**
 * @author 杨超辉
 * @param <T> 实体类
 * @param <S> Q实体类
 * repository基类
 */
@NoRepositoryBean
public interface BaseRepository<T, S extends EntityPath<?>> extends JpaRepository<T, String>,
        QuerydslPredicateExecutor<T>, QuerydslBinderCustomizer<S>, JpaSpecificationExecutor<T>, ExtendsRepository<T, String, S> {

    @Override
    default void customize(@NonNull QuerydslBindings bindings, @NonNull S entityPath) {
        DefaultQuerydslBinder.customize(bindings, entityPath);
    }

    /**
     * 多数情况使用exists时都是判断数据唯一性，故直接默认加悲观锁，如特殊情况需取消悲观锁，继承覆盖即可
     * @param predicate
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional
    boolean exists(Predicate predicate);
}
