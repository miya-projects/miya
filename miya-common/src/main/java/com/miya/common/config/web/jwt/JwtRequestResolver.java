package com.miya.common.config.web.jwt;

import cn.hutool.core.util.StrUtil;
import com.miya.common.auth.way.GeneralAuthentication;
import com.miya.common.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 杨超辉
 * 请求权限解析器
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtRequestResolver {

    private final static Pattern REG = Pattern.compile("Bearer (.+)");

    private final JwtTokenService jwtTokenService;

    /**
     * 认证一个请求
     * @param request
     */
    public Authentication getAuthentication(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = resolveAuthorization(authorization);
        if (StrUtil.isBlank(token) && request.getMethod().equalsIgnoreCase("GET")){
            token = request.getParameter("token");
        }
        if (Objects.isNull(token)){
            return null;
        }
        return getAuthentication(token);
    }

    /**
     * 解析authorization头 获得token
     * @param authorization
     * @return token
     */
    public String resolveAuthorization(String authorization){
        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        Matcher matcher = REG.matcher(authorization);
        boolean matches = matcher.matches();
        if (!matches){
            return null;
        }
        return matcher.group(1);
    }

    /**
     * 将token解析为authentication  解析无状态token
     * @param token
     */
    public Authentication getAuthentication(String token) {
        GeneralAuthentication generalAuthentication;
        try {
            JwtPayload payload = jwtTokenService.getPayload(token);
            generalAuthentication = GeneralAuthentication.getFromToken(payload);
            generalAuthentication.setUser(jwtTokenService.getUserByToken(token));
            generalAuthentication.setAuthenticated(true);
        }catch (Exception e){
            log.trace(ExceptionUtils.getStackTrace(e));
            log.debug("token认证不通过, " + token);
            return null;
        }
        return generalAuthentication;
    }

}

