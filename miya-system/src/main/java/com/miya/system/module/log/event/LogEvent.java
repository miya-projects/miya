package com.miya.system.module.log.event;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.util.AuthenticationUtil;
import com.miya.system.config.business.Business;
import com.miya.system.module.role.SysRoleService;
import com.miya.system.module.user.model.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 发生该事件后，日志模块将监听到该事件并记录一条日志
 * 通过 applicationContext.publishEvent 触发
 */
@Getter
public class LogEvent extends ApplicationEvent {

    private final String content;
    private final String operationType;
    private final String businessId;
    private final Business business;
    private final String operatorName;
    private Map<String, Object> extra = MapUtil.empty();

    public LogEvent(String content, String operationType, String businessId) {
        super(new Object());
        this.content = content;
        this.operationType = operationType;
        this.businessId = businessId;
        this.business = inferBusiness();
        this.operatorName = inferUser();
    }

    public LogEvent(String content, String operationType, String businessId, Map<String, Object> extra) {
        super(new Object());
        this.content = content;
        this.operationType = operationType;
        this.businessId = businessId;
        this.extra = extra;
        this.business = inferBusiness();
        this.operatorName = inferUser();
    }


    /**
     * 推断所属业务模块
     */
    private Business inferBusiness(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(servletRequestAttributes)){
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object businessAttr = request.getAttribute("business");
            return SpringUtil.getBean(SysRoleService.class).valueOfCode(Optional.ofNullable(businessAttr).map(Object::toString).orElse(""));
        }
        return null;
    }


    /**
     * 推断操作用户
     */
    private String inferUser(){
        Object principal = AuthenticationUtil.getPrincipal();
        if (principal == null){
            return null;
        }
        if (principal instanceof SysUser){
            return ((SysUser)principal).getName();
        }
        Object name = ReflectUtil.getFieldValue(principal, "name");
        if (name == null){
            return null;
        }
        return name.toString();
    }
}
