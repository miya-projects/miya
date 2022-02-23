package com.miya.common.module.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

/**
 * service基类
 */
@Slf4j
public class BaseService {

    @Setter
    @Resource
    protected JPAQueryFactory qf;

    @Setter
    @Resource
    protected ApplicationContext ac;

}
