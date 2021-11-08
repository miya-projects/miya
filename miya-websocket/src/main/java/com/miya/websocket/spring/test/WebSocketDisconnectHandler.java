package com.miya.websocket.spring.test;

import java.util.Collections;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

public class WebSocketDisconnectHandler<S> implements ApplicationListener<SessionDisconnectEvent> {

	private final ActiveWebSocketUserService service;

	private final SimpMessageSendingOperations messagingTemplate;

	public WebSocketDisconnectHandler(SimpMessageSendingOperations messagingTemplate,
									  ActiveWebSocketUserService service) {
		super();
		this.messagingTemplate = messagingTemplate;
		this.service = service;
	}

	@Override
	public void onApplicationEvent(SessionDisconnectEvent event) {
		String id = event.getSessionId();
		if (id == null) {
			return;
		}
		ActiveWebSocketUser user = this.service.getBySessionId(id);
		this.messagingTemplate.convertAndSend("/topic/friends/signout",
		Collections.singletonList(user.getUsername()));
	}

}
