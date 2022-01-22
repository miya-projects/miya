package com.miya.common.module.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * service基类
 */
@Slf4j
public class BaseService {

    @Resource
    protected JPAQueryFactory qf;

}
