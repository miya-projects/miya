package com.miya.common.module.config;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.data.util.CastUtils;

import java.io.Serializable;
import java.util.Optional;

public interface SystemConfig extends Serializable {

    String name();

    String getName();

    /**
     * 值的类型
     */
    Class getValueType();

    /**
     * 默认值
     */
    String getDefaultValue();

    /**
     * 获取配置项的值，如果不存在则创建并返回默认值
     * @return
     * @param <T>
     */
    default <T> T getValue() {
        SysConfigService service = SpringUtil.getBean(SysConfigService.class);
        Optional optional = service.get(name(), getValueType());
        if (optional.isEmpty()) {
            service.touchSystemConfig(this);
            return getValue();
        }
        return CastUtils.cast(optional.get());
    }
}
