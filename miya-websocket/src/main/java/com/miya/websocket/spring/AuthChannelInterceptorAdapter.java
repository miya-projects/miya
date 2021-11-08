package com.miya.websocket.spring;

import com.miya.common.config.web.jwt.JwtRequestResolver;
import com.miya.common.module.cache.KeyValueStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.Objects;

@Slf4j
@Service
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    // @Resource
    // private WebSocketTokenService webSocketTokenService;
    //
    @Resource
    private JwtRequestResolver jwtRequestResolver;
    // private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    private KeyValueStore keyValueStore;

    // public AuthChannelInterceptorAdapter(final WebSocketAuthenticatorService webSocketAuthenticatorService) {
    //     this.webSocketAuthenticatorService = webSocketAuthenticatorService;
    // }

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) throws AuthenticationException {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            Authentication authentication = jwtRequestResolver.getAuthentication(jwtRequestResolver
                    .resolveAuthorization(accessor.getFirstNativeHeader("Authorization")));
            if (Objects.isNull(authentication)){
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof Principal){
                accessor.setUser((Principal) principal);
            }else {
                accessor.setUser(authentication);
            }
        }
        return message;
    }
}
