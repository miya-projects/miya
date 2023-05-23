package com.teamytd.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class WebsocketHeartbeat {

    @Scheduled(fixedRate = 1000 * 60)
    public void heartbeat() {
        WebsocketSessionHolder.sendAll("");
    }

}
