package com.miya.common.config.web;

import com.miya.common.model.dto.base.Grid;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 配置RepeatableRequestMappingHandlerMapping
 */
@Component
public class WebRequestMappingConfig implements WebMvcRegistrations, EnvironmentAware {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RepeatableRequestMappingHandlerMapping();
        handlerMapping.setOrder(0);
        return handlerMapping;
    }

    /**
     * 配置Grid类分页是否从0开始
     * @param environment 环境变量
     */
    @Override
    public void setEnvironment(Environment environment) {
        // {org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties}
        Grid.oneIndexedParameters = environment.getProperty("spring.data.web.pageable.one-indexed-parameters", Boolean.class, false);
    }
}
