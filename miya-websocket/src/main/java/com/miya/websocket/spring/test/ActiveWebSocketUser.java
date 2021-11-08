package com.miya.websocket.spring.test;


import java.util.Calendar;


public class ActiveWebSocketUser {

    private String id;

    private String username;

    private Calendar connectionTime;

    public ActiveWebSocketUser() {
    }

    public ActiveWebSocketUser(String id, String username, Calendar connectionTime) {
        super();
        this.id = id;
        this.username = username;
        this.connectionTime = connectionTime;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Calendar getConnectionTime() {
        return this.connectionTime;
    }

    public void setConnectionTime(Calendar connectionTime) {
        this.connectionTime = connectionTime;
    }
}
