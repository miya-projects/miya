package com.miya.system.module.search;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Slf4j
@Configuration
@ConditionalOnClass(name = "org.hibernate.search.backend.elasticsearch.impl.ElasticsearchBeanConfigurer")
public class SysSearchConfig implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Bean
    public SysSearchService searchService(){
        return new SysSearchService(entityManager);
    }

    @Bean
    public SysSearchApi searchApi(SysSearchService searchService){
        return new SysSearchApi(searchService);
    }
}
