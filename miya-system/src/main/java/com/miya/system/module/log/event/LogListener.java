package com.miya.system.module.log.event;

import com.miya.system.module.log.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogListener implements ApplicationListener<LogEvent> {

    private final LogService logService;

    @Async
    @Override
    public void onApplicationEvent(LogEvent event) {
        logService.log(event.getContent(), event.getOperationType(), event.getBusinessId(), event.getExtra());
    }
}
