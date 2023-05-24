package com.miya.system.module.user.event;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.miya.system.module.log.event.LogEvent;
import com.miya.system.module.user.model.SysUser;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户登录事件
 */
public class UserLoginEvent extends LogEvent {

    private UserLoginEvent(SysUser sysUser, Map<String, Object> extra) {
        super(StrUtil.format("{}登录成功", sysUser.getName()), "用户登录", sysUser.getId(), extra);
    }

    @Accessors(fluent = true)
    public static class Builder{

        @Setter
        private SysUser user;

        public UserLoginEvent build(){
            assert user != null;
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String clientIP = JakartaServletUtil.getClientIP(request);
            // 获取访问浏览器、操作系统
            UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
            Map<String, Object> extra = MapUtil.<String, Object>builder().put("ip", clientIP)
                    .put("userAgent", userAgent.getBrowser().getName())
                    .put("os", userAgent.getOs().getName())
                    .build();
            return new UserLoginEvent(user, extra);
        }

    }

}
