package com.miya.common.module.base;

import com.miya.common.model.dto.base.Grid;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * 扩展一些repository功能
 * todo
 * @param <T>
 * @param <ID>
 */
public class ExtendsRepositoryImpl<T, ID, S extends EntityPath<?>> extends SimpleJpaRepository<T, ID> implements ExtendsRepository <T, ID, S> {

    private final EntityManager entityManager;
    private final JPAQueryFactory qf;
    EntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
    private final EntityPath<T> entityPath;

//    ExtendsRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, JpaContext context) {
//        super(entityInformation, context.getEntityManagerByManagedType(entityInformation.getJavaType()));
//        this.entityManager = context.getEntityManagerByManagedType(entityInformation.getJavaType());
//        this.qf = new JPAQueryFactory(entityManager);
//        this.entityPath = resolver.createPath(entityInformation.getJavaType());
//    }

    ExtendsRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.qf = new JPAQueryFactory(entityManager);
        this.entityPath = resolver.createPath(entityInformation.getJavaType());
    }

    @Override
    public <D> List<D> findAll(Predicate predicate, Class<D> clazz){
        // todo 还不能用
        return qf.select(Projections.bean(clazz, entityPath)).from(entityPath).where(predicate).fetch();
    }

    @Override
    public <D> Grid<D> findAll(Predicate predicate, Pageable pageable, Class<D> clazz) {
        return null;
    }

//    @Override
//    public <D> Grid<D> findAll(Predicate predicate, Pageable pageable, Class<D> clazz){
//        // todo 还不能用
//        QueryResults<D> dQueryResults = queryFactory.select(Projections.bean(clazz, entityPath))
//                .from(entityPath)
//                .where(predicate)
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetchResults();
//        return Grid.of(dQueryResults);
//    }

}
