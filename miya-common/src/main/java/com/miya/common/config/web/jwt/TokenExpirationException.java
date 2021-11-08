package com.miya.common.config.web.jwt;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * token失效异常
 */
@Getter
@Setter
public class TokenExpirationException extends Exception{

    private String token;

    public TokenExpirationException(String token){
        super(StrUtil.format("token失效:{}", token));
        this.token = token;
    }


}
