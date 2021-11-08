package com.miya.common.module.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * service基类
 */
@Slf4j
public class BaseService {

    @PersistenceContext
    protected EntityManager entityManager;

    protected JPAQueryFactory qf;

    @PostConstruct
    public void init() {
        qf = new JPAQueryFactory(entityManager);
    }

}
