package com.miya.websocket.spring.bak;

import com.miya.websocket.spring.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ws服务 给客户端发消息时，注入此类即可
 */
// @Component
@Slf4j
public class WebSocketService {

    /**
     * 这个会出现性能问题，最好用Map来存储，key用userid
     */
    static final Map<Serializable, WebSocketSession> USER_MAP;

    static {
        USER_MAP = new ConcurrentHashMap<>();
    }

    /**
     * 给某个用户发送消息
     * @param userId    用户id
     * @param userType  用户class
     * @param message   要发送的消息
     * @return
     */
    public boolean sendMessageToUser(Serializable userId, Class userType, WebSocketMessage message) {
        WebSocketSession session = USER_MAP.get(userId);
        if (Objects.isNull(session)){
            log.debug("没有找到要推送消息的用户或者该用户不在线");
            return false;
        }
        Object user = ((Authentication) Objects.requireNonNull(session.getPrincipal())).getDetails();
        try {
            Method getId = user.getClass().getMethod("getId");
            String id = getId.invoke(user).toString();
            if (session.isOpen() && id.equals(userId)) {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message.toJSONString()));
                }
            }
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 给所有在线用户发送消息 考虑多端、多设备、多用户类型 谨慎使用
     * @param message
     */
    public void sendMessageToUsers(WebSocketMessage message) {
        for (WebSocketSession user : USER_MAP.values()) {
            try {
                if (user.isOpen()) {
                    user.sendMessage(new TextMessage(message.toJSONString()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
