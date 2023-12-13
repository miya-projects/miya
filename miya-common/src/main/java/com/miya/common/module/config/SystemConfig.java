package com.miya.common.module.config;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.util.CastUtils;

import java.io.Serializable;

/**
 * 任何需要存储配置的模块建议实现该接口作为枚举，并在系统初始化时给予默认配置，
 */
public interface SystemConfig extends Serializable {

    String name();

    String getName();

    /**
     * 值的类型
     */
    Class<?> getValueType();

    /**
     * 默认值
     */
    String getDefaultValue();

    /**
     * 所属分组
     */
    String group();

    /**
     * 获取配置项的值，如果不存在则创建并返回默认值
     */
    default <T> T getValue() {
        SysConfigService service = SpringUtil.getBean(SysConfigService.class);
        Object t = service.get(group(), name(), getValueType());
        if (t == null) {
            service.touchSystemConfig(this);
            return CastUtils.cast(DefaultConversionService.getSharedInstance().convert(getDefaultValue(), getValueType()));
        }
        return CastUtils.cast(t);
    }
}
