package com.miya.common.config.web;

import com.miya.common.module.specificationbinds.DefaultQueryBindingConfigurator;
import com.miya.common.module.specificationbinds.DefaultSpecificationBinder;
import com.miya.common.module.specificationbinds.QueryBindingConfigurator;
import com.miya.common.module.specificationbinds.SpecificationBinderCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import java.util.List;

@Configuration
public class SpecificationConfig {


    @Bean
    @ConditionalOnMissingBean(QueryBindingConfigurator.class)
    public DefaultQueryBindingConfigurator defaultQueryBindingConfigurator(List<SpecificationBinderCustomizer<?>> specificationBinderCustomizers) {
        return new DefaultQueryBindingConfigurator(specificationBinderCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultSpecificationBinder.class)
    public DefaultSpecificationBinder defaultSpecificationBinder(@Qualifier("mvcConversionService") ConversionService conversionService) {
        return new DefaultSpecificationBinder(conversionService);
    }

}
