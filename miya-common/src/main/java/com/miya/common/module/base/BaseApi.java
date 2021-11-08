package com.miya.common.module.base;

import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author 杨超辉
 */
public class BaseApi {

    @Resource
    @PersistenceContext
    protected EntityManager entityManager;

    protected JPAQueryFactory qf;

    @PostConstruct
    public void init() {
        qf = new JPAQueryFactory(entityManager);
    }


}
