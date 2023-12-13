package com.miya.common.module.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统元信息，第一次访问首页时加载
 */
@AllArgsConstructor
@Getter
public class SystemMeta {
    private final String systemName;
    private final String version;
    private final String exportWay;
}
