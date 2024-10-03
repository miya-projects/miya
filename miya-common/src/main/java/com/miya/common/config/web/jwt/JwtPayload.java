package com.miya.common.config.web.jwt;

import cn.hutool.core.bean.BeanUtil;
import com.miya.common.auth.way.LoginDevice;
import com.miya.common.auth.way.LoginWay;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * token payload
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtPayload {
    /**
     * 用户对象类
     */
    private Class<?> userClass;
    /**
     * 用户id
     */
    private Serializable userId;
    /**
     * 登录设备
     */
    private LoginDevice loginDevice;
    /**
     * 登录方式
     */
    private LoginWay loginWay;
    /**
     * 登录时间
     */
    private Date loginTime;
    /**
     * token失效时间
     */
    private Date exp;


    //<a href="https://github.com/jwtk/jjwt/issues/122">issue</a>
    public Map<String, Object> toClaims() {
        Map<String, Object> map = BeanUtil.beanToMap(this);
        if (userClass != null) {
            // 在生成token时，不同序列化框架对class可能有不同的表现，尽量使用简单类型
            map.put("userClass", userClass.getName());
        }
        return map;
    }
}
