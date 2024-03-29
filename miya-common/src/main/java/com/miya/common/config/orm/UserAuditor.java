package com.miya.common.config.orm;

import cn.hutool.core.util.ReflectUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Optional;

/**
 * 为数据审计提供操作数据的用户
 */
@Configuration
@Slf4j
public class UserAuditor implements AuditorAware<String> {

    /**
     * 获取当前创建或修改的用户
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            ServletRequestAttributes servletRequestAttributes =  (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                return Optional.empty();
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object principal = request.getAttribute("principal");
            if(principal instanceof String ){
                return Optional.of(principal.toString());
            }
            boolean hasName = ReflectUtil.hasField(principal.getClass(), "name");
            if(hasName){
                String name = (String)ReflectUtil.getFieldValue(principal, "name");
                return Optional.of(name);
            }else {
                return Optional.of(principal.getClass().toString());
            }
        }catch (Exception e){
            return Optional.empty();
        }
    }
}
