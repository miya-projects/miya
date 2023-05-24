package com.teamytd.ws;

import com.miya.system.module.user.model.SysUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WebsocketSessionHolder {

    static {
        sessions = new HashMap<>();
    }

    // key - userId, value - List of user's sessions
    private static final Map<String, List<WebSocketSession>> sessions;

    public static void addSession(String userId, WebSocketSession session) {
        synchronized (sessions) {
            var userSessions = sessions.get(userId);
            if (userSessions == null) {
                userSessions = new ArrayList<WebSocketSession>();
            }
            userSessions.add(session);
            sessions.put(userId, userSessions);
            log.debug("用户{}连接, sessionId: {}", userId, session.getId());
            try {
                session.sendMessage(new TextMessage("连接成功"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void closeSessions(WebSocketSession session) throws IOException {
        SysUserPrincipal sysUserPrincipal = CustomWebSocketHandler.getSysUserPrincipal(session);
        if (sysUserPrincipal == null) {
            return;
        }
        synchronized (sessions) {
            var userSessions = sessions.get(sysUserPrincipal.getId());
            if (userSessions != null) {
                for (var s : userSessions) {
                    if (s.equals(session)) {
                        // I use POLICY_VIOLATION to indicate reason of disconnecting for a client
                        s.close(CloseStatus.POLICY_VIOLATION);
                        userSessions.remove(s);
                        log.debug("用户{}退出连接, sessionId: {}", sysUserPrincipal.getId(), session.getId());
                        return;
                    }
                }
            }
        }
    }

    public static void closeSessions(String userId) throws IOException {
        synchronized (sessions) {
            var userSessions = sessions.get(userId);
            if (userSessions != null) {
                for (var session : userSessions) {
                    // I use POLICY_VIOLATION to indicate reason of disconnecting for a client
                    session.close(CloseStatus.POLICY_VIOLATION);
                    log.debug("用户{}退出连接, sessionId: {}", userId, session.getId());
                }
                sessions.remove(userId);
            }
        }
    }

    public static Map<String, List<WebSocketSession>> getSessions() {
        return sessions;
    }

    public static void sendAll(String message) {
        for (var userSessions : sessions.values()) {
            for (var session : userSessions) {
                synchronized (session.getId().intern()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error(ExceptionUtils.getStackTrace(e));
                    }
                }
            }
        }
    }

    public static void sendToUser(String userId, String message) {
        List<WebSocketSession> webSocketSessions = sessions.get(userId);
        if (webSocketSessions == null) {
            return;
        }
        for (var session : webSocketSessions) {
            synchronized (session.getId().intern()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error(ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

}
