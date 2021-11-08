package com.miya.websocket.spring.test;


/**
 * 在线用户服务 websocket
 */
public interface ActiveWebSocketUserService {

    void save(ActiveWebSocketUser user);

    ActiveWebSocketUser getBySessionId(String id);
}
