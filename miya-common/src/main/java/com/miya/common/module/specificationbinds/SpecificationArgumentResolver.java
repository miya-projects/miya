package com.miya.common.module.specificationbinds;


import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.util.Map;

/**
 * controller中Specification参数解析器
 */
public class SpecificationArgumentResolver implements HandlerMethodArgumentResolver {

    private final QueryBindingConfigurator configurator;

    public SpecificationArgumentResolver(QueryBindingConfigurator configurator) {
        this.configurator = configurator;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Specification.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        Map<String, String[]> parameterMap = webRequest.getParameterMap();
        Class<?> entityClass = getEntityClassFromSpecification(parameter);
        return new EntitySpecification<>(configurator.getBindingsForEntity(entityClass), parameterMap);

    }

    private Class<?> getEntityClassFromSpecification(MethodParameter parameter) {
        return (Class<?>) ((java.lang.reflect.ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[0];
    }
}
