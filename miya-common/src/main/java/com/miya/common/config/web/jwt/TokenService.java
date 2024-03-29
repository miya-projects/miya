package com.miya.common.config.web.jwt;

import lombok.NonNull;

import java.io.Serializable;
import java.util.Date;

public interface TokenService {

    /**
     * 根据token获取用户对象
     * @param token
     * @return 用户对象
     */
    Object parseUserFromToken(String token);

    /**
     * 刷新token有效时间
     * @param token 旧token
     * @param expirationDate    有效期至
     * @return token
     */
    String reFlushToken(String token, Date expirationDate) throws TokenExpirationException;

    /**
     * 获取token 并将用户对象设置到tokenStore
     * @param jwtPayload
     * @param userPrincipal       用户对象
     * @return token
     */
    String generateToken(@NonNull JwtPayload jwtPayload, @NonNull Serializable userPrincipal);
}
