package com.miya.common.module.config;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SystemConfigKeys implements SystemConfig {
    IS_INITIALIZE("是否初始化完毕", Boolean.class, "false"),
    SYSTEM_NAME("系统名称", String.class, "MIYA"),
    SYSTEM_VERSION("系统版本", String.class, "0.0.1"),
    BACKEND_DOMAIN("后端域名(让后端知道怎么可以访问到自己)", String.class, "http://localhost:8080"),
    OSS_DOMAIN("OSS域名，设置后文件访问将直接使用该域名，需自行配置OSS后端", String.class, ""),
    EXPORT_WAY("文件导出方式，async(异步)sync(同步), 默认同步", String.class, "sync"),
    JWT_SECRET_KEY("jwt密钥(请勿泄露)", String.class, RandomUtil.randomString(18)),
    ;

    private final String name;

    /**
     * 值的类型
     */
    private final Class valueType;
    /**
     * 默认值
     */
    private final String defaultValue;

    // public Object getValue() {
    //     SysConfigService configService = SpringUtil.getBean(SysConfigService.class);
    //     // 不想给默认值 -> 应当有一次初始化
    //     return configService.get(this);
    // }
}
