package com.teamytd.ws;

import com.miya.system.module.user.model.SysUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import java.security.Principal;

@Slf4j
public class CustomWebSocketHandler extends AbstractWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        SysUserPrincipal sysUserPrincipal = getSysUserPrincipal(session);
        if (sysUserPrincipal == null) {
            return;
        }
        WebsocketSessionHolder.addSession(sysUserPrincipal.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SysUserPrincipal sysUserPrincipal = getSysUserPrincipal(session);
        if (sysUserPrincipal == null) {
            return;
        }
        WebsocketSessionHolder.closeSessions(session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (message.getPayload().equals("")) {
            // 心跳
            return;
        }
        log.info("收到消息: {}", message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        exception.printStackTrace();
    }


    @Nullable
    public static SysUserPrincipal getSysUserPrincipal(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal == null) {
            log.error("principal is null");
            return null;
        }
        if (!(principal instanceof SysUserPrincipal)) {
            log.error("principal is not SysUserPrincipal");
            return null;
        }
        SysUserPrincipal sysUserPrincipal = (SysUserPrincipal) principal;
        return sysUserPrincipal;
    }



}
