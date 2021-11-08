package com.miya.system.listener.event;

import org.springframework.context.ApplicationEvent;

/**
 * 系统启动事件
 * 通过 applicationContext.publishEvent 触发
 */
public class SystemStartupEvent extends ApplicationEvent {

    public SystemStartupEvent(){
        super(new Object());
    }

}
