package com.miya.common.config.web.jwt;

import lombok.NonNull;

import java.io.Serializable;
import java.util.Date;

public interface TokenService {

    /**
     * 根据token获取用户对象
     * @param token
     * @return
     */
    Object getUserByToken(String token);

    /**
     * 刷新token有效时间
     * @param token 旧token
     * @param expirationDate    有效期至
     * @return
     */
    String reFlushToken(String token, Date expirationDate) throws TokenExpirationException;

    /**
     * 获取token 并将用户对象设置到tokenStore
     * @param jwtPayload
     * @param user  用户对象
     * @param expirationDate    token失效时间
     * @return
     */
    String generateToken(@NonNull JwtPayload jwtPayload, @NonNull Serializable user, Date expirationDate);
}
