package com.miya.system.module.config;

import com.miya.common.module.base.Convertable;
import com.miya.common.module.config.SysConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class SysConfigDTO extends Convertable {

    /**
     * 变量key
     */
    private String key;

    /**
     * 变量值
     */
    private String val;

    /**
     * 配置项说明
     */
    private String desc;

    /**
     * 分组
     */
    private String group;

    public static SysConfigDTO of(SysConfig sysConfig) {
        return modelMapper.map(sysConfig, SysConfigDTO.class);
    }

}
