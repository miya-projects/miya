package com.miya.websocket.spring.test;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ActiveUserInDbService implements ActiveWebSocketUserService{

    private final List<ActiveWebSocketUser> list = new ArrayList<>();

    @Override
    public void save(ActiveWebSocketUser user) {
        list.add(user);
    }

    @Override
    public ActiveWebSocketUser getBySessionId(String id) {
        return list.stream().findAny().get();
    }
}
