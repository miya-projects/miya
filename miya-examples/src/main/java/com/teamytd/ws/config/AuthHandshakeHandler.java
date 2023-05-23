package com.teamytd.ws.config;

import cn.hutool.core.net.url.UrlBuilder;
import com.miya.common.auth.way.GeneralAuthentication;
import com.miya.common.config.web.jwt.JwtRequestResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 握手时进行用户身份认证，抛出异常后即握手失败
 */
@RequiredArgsConstructor
public class AuthHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtRequestResolver jwtRequestResolver;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        CharSequence token = UrlBuilder.of(Objects.requireNonNull(request.getURI()),
                Charset.defaultCharset()).getQuery().get("token");
        GeneralAuthentication authentication = jwtRequestResolver.getAuthentication(Optional.ofNullable(token).map(CharSequence::toString).orElse(""));
        if (authentication == null) {
            throw new RuntimeException("用户未登录");
        }
        return (Principal)authentication.getUser();
    }
}
