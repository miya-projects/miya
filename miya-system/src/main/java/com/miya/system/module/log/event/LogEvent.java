package com.miya.system.module.log.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.Map;

/**
 * 发生该事件后，日志模块将监听到该事件并记录一条日志
 * 通过 applicationContext.publishEvent 触发
 */
@Getter
public class LogEvent extends ApplicationEvent {

    private final String content;
    private final String operationType;
    private final String businessId;
    private final Map<String, Object> extra;

    public LogEvent(String content, String operationType, String businessId, Map<String, Object> extra) {
        super(new Object());
        this.content = content;
        this.operationType = operationType;
        this.businessId = businessId;
        this.extra = extra;
    }

}
