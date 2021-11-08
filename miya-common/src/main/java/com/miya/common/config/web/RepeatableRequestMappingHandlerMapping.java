package com.miya.common.config.web;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可重复注册的requestMappingHandlerMapping，实现覆盖jar包中接口的目的
 * 同一个requestMappingInfo,(url method, consumer等等要一致), 在接口上增加@Order，指定的数字越小，优先级越高，优先级低的接口将被取消注册。
 */
public class RepeatableRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final Map<RequestMappingInfo, Method> mappings = new ConcurrentHashMap<>();

    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        if (this.mappings.containsKey(mapping)) {
            int order = getOrder(method);
            Method alreadyMapping = this.mappings.get(mapping);
            int alreadyOrder = getOrder(alreadyMapping);
            if (order == alreadyOrder){
                throw new RuntimeException("重复注册了requestMapping, " + mapping);
            }
            if (order > alreadyOrder){
                // 不注册该映射
                return;
            }
            // 否则取消掉之前的映射，注册现在的
            unregisterMapping(mapping);
        }
        mappings.put(mapping, method);
        super.registerHandlerMethod(handler, method, mapping);
    }

    private int getOrder(Method method){
        Order order = AnnotationUtils.findAnnotation(method, Order.class);
        return Optional.ofNullable(order).map(Order::value).orElse(0);
    }

}
