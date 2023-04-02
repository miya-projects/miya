package com.miya.common.config.web;

import com.miya.common.annotation.Acl;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * springmvc 提供相关的能力
 */
@Service
public class SpringMvcService {

    //无需权限可访问的url
    private static final String[] ALLOW_ACCESS_URL = {
            "/api/logout", "/websocket"
    };

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 获取允许不登录访问的url
     * 配置了该注解@Acl(userType = Acl.NotNeedLogin)的class或method，或在ALLOW_ACCESS_URL内的url
     * @see  Acl
     */
    public String[] allowAccessUrlForAcl(){
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        return allowAccessUrlForAcl(handlerMethods);
    }

    /**
     * 获取允许不登录访问的url
     * 配置了该注解@Acl(userType = Acl.NotNeedLogin)的class或method，或在ALLOW_ACCESS_URL内的url
     * @see  Acl
     */
    public String[] allowAccessUrlForAcl(Map<RequestMappingInfo, HandlerMethod> handlerMethods){
        Set<Map.Entry<RequestMappingInfo, HandlerMethod>> entries = handlerMethods.entrySet();
        Set<String> urls = new HashSet<>();
        entries.forEach( entry -> {
            RequestMappingInfo key = entry.getKey();
            HandlerMethod value = entry.getValue();

            Set<String> patterns = new HashSet<>();
            PatternsRequestCondition patternsCondition = key.getPatternsCondition();
            if (patternsCondition != null){
                patterns = patternsCondition.getPatterns();
            }
            Class<?> beanType = value.getBeanType();
            Acl aclForClass = AnnotationUtils.findAnnotation(beanType, Acl.class);
            Acl aclForMethod = AnnotationUtils.findAnnotation(value.getMethod(), Acl.class);
            if (Objects.nonNull(aclForMethod)){
                if(Acl.NotNeedLogin.class.equals(aclForMethod.userType())){
                    urls.addAll(patterns);
                }
            }
            if (Objects.nonNull(aclForClass)){
                if(Acl.NotNeedLogin.class.equals(aclForClass.userType())){
                    urls.addAll(patterns);
                }
            }
        });
        urls.addAll(Arrays.asList(ALLOW_ACCESS_URL));
        return urls.toArray(new String[0]);
    }
}
