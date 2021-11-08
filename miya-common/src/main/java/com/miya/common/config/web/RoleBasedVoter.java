package com.miya.common.config.web;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;

/**
 * 基于角色的投票者
 */
@Slf4j
//@Component
public class RoleBasedVoter implements AccessDecisionVoter<Object> {

    @Resource
    private ApplicationContext applicationContext;
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @SneakyThrows
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        if(authentication == null) {
            return ACCESS_DENIED;
        }
        int result = ACCESS_ABSTAIN;

        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        if(object instanceof FilterInvocation){
            HttpServletRequest httpRequest = ((FilterInvocation) object).getHttpRequest();
            HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(httpRequest);
            if(Objects.nonNull(handlerExecutionChain) && handlerExecutionChain.getHandler() instanceof HandlerMethod){
                HandlerMethod handlerMethod = (HandlerMethod)handlerExecutionChain.getHandler();

                Collection<? extends GrantedAuthority> authorities = extractAuthorities(authentication);

                for (ConfigAttribute attribute : attributes) {
                    if(attribute.getAttribute()==null){
                        continue;
                    }
                    if (this.supports(attribute)) {
                        result = ACCESS_DENIED;

                        // Attempt to find a matching granted authority
                        for (GrantedAuthority authority : authorities) {
                            if (attribute.getAttribute().equals(authority.getAuthority())) {
                                return ACCESS_GRANTED;
                            }
                        }
                    }
                }

            }
        }

        return result;
    }

    Collection<? extends GrantedAuthority> extractAuthorities(
            Authentication authentication) {
        return authentication.getAuthorities();
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }

}
