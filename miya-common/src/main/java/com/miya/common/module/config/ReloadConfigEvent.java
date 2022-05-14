package com.miya.common.module.config;

import org.springframework.context.ApplicationEvent;

/**
 * 重载系统配置
 */
public class ReloadConfigEvent extends ApplicationEvent {

    public ReloadConfigEvent() {
        super(new Object());
    }
}
