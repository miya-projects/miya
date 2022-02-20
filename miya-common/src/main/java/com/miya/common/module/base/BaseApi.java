package com.miya.common.module.base;

import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.annotation.Resource;

/**
 * @author 杨超辉
 */
public class BaseApi {

    @Resource
    protected JPAQueryFactory qf;

}
