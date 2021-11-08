package com.miya.websocket.spring;

import com.miya.common.auth.way.GeneralAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import javax.annotation.Resource;

@Slf4j
@Controller
public class WebSocketController {

    @Resource
    private SimpMessagingTemplate template;

    @MessageMapping("/hello")
    @SendTo("/topic/notice")
    public WebSocketMessage greeting(String name) throws Exception {
        log.info(name);
        return WebSocketMessage.of("hello " + name);
    }

    @MessageMapping("/send")
    public void greeting(String name, String sex, GeneralAuthentication principal, MessageHeaderAccessor obj) throws Exception {
        Object principal1 = principal.getPrincipal();
        principal.getName();
        String name1 = principal.getName();
        log.info("name: {}, sex: {}", name, sex);
        template.convertAndSendToUser("", "/topic/notice", "halou");
    }

    /**
     * 异常处理
     * @param exception
     * @return
     */
    @MessageExceptionHandler
    @SendToUser(destinations="/queue/errors", broadcast=false)
    public WebSocketMessage handleException(RuntimeException exception) {
        return WebSocketMessage.of(ExceptionUtils.getMessage(exception));
    }
}
