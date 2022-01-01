package com.miya.common.config.orm;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

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
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
