package com.miya.common.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.RegisteredPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miya.common.auth.way.LoginDevice;
import com.miya.common.auth.way.LoginWay;
import com.miya.common.config.web.jwt.JwtPayload;
import com.miya.common.config.web.jwt.TokenExpirationException;
import com.miya.common.config.web.jwt.TokenService;
import com.miya.common.module.cache.CacheKey;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.common.module.config.SystemConfigKeys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author 杨超辉
 */
@Slf4j
public class JwtTokenService implements Serializable, TokenService, ApplicationContextAware, SmartInitializingSingleton {

    @Serial
    private static final long serialVersionUID = -3301605591108950415L;

    /**
     * 签名密钥
     */
    private byte[] secret;

    private final KeyValueStore keyValueStore;

    private static JwtTokenService INSTANCE;


    public JwtTokenService(KeyValueStore keyValueStore){
        this.keyValueStore = keyValueStore;
    }


    /**
     * 获取payload 如token不合法，返回null
     * @param token
     */
    public JwtPayload getPayload(String token) {
        Map<String, Object> claims = getClaimsFromToken(token);
        try {
            ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
            return mapper.readValue(JSONUtil.toJsonStr(claims), JwtPayload.class);
        } catch (Exception e) {
            log.trace("token不合法: {}", ExceptionUtils.getStackTrace(e));
            // throw new RuntimeException(StrUtil.format("token 不合法: {}", token));
            return null;
        }
    }


    /**
     * 获取token中的过期时间
     * @param token
     */
    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Map<String, Object> claims = getClaimsFromToken(token);
            expiration = Convert.toDate(claims.get(RegisteredPayload.EXPIRES_AT));
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    /**
     * 获取token中存的信息 payload
     * @param token
     */
    private Map<String, Object> getClaimsFromToken(String token) {
        JWT jwt = JWT.of(token).setKey(secret);
        if (!jwt.verify()) {
            log.warn("jwt验证不通过: {}", token);
            return null;
        }
        return jwt.getPayloads();
    }


    /**
     * token是否过期？
     * @param token
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    /**
     * 生成token
     * @param jwtPayload    token payload
     */
    public String payloadToToken(JwtPayload jwtPayload) {
        Map<String, Object> claims = jwtPayload.toClaims();
        return JWT.create()
                .addPayloads(claims)
                .setKey(secret)
                .sign();
    }

    public static void main(String[] args) {
        JwtPayload payload = JwtPayload.builder().userClass(String.class)
                .exp(new Date())
                .loginWay(LoginWay.WXCODE)
                .loginDevice(LoginDevice.MOBILE)
                .loginTime(new Date()).userId("1")
                .build();
        JwtTokenService service = new JwtTokenService(null);

        byte[] key = "1234567890".getBytes();

        //https://datatracker.ietf.org/doc/html/rfc7519#page-6
        service.secret = key;
        String s = service.payloadToToken(payload);
        // 密钥

        String token = JWT.create()
                .addPayloads(payload.toClaims())
                .setKey(key)
                .sign();
        System.out.println(token);
        System.out.println(s);
    }

    /**
     * 刷新token
     * @param token
     * @param expirationDate 过期日期
     */
    public String refreshToken(String token, Date expirationDate) {
        String refreshedToken;
        try {
            JwtPayload payload = getPayload(token);
            payload.setExp(expirationDate);
            refreshedToken = payloadToToken(payload);
        } catch (Exception e) {
            e.printStackTrace();
            refreshedToken = null;
        }
        return refreshedToken;
    }


    Repositories repositories;
    DefaultRepositoryInvokerFactory defaultRepositoryInvokerFactory;

    /**
     * 根据jwtPayload获取用户对象
     * @param jwtPayload
     */
    @Transactional
    public Object getUserByJwtPayload(JwtPayload jwtPayload) {
        Class<?> userPrincipalClass = jwtPayload.getUserClass();
        Class userClass = ReflectUtil.invokeStatic(ReflectUtil.getMethod(userPrincipalClass, "userType"));
        RepositoryInvoker invokerFor = defaultRepositoryInvokerFactory.getInvokerFor(userClass);
        Optional<Object> userOptional = invokerFor.invokeFindById(jwtPayload.getUserId());
        Object user = userOptional.orElse(null);
        if (user != null) {
            Method method = ReflectUtil.getMethod(userPrincipalClass, "of", userClass);
            return ReflectUtil.invokeStatic(method, user);
        }
        return user;
    }

    @Override
    public Object parseUserFromToken(String token){
        JwtPayload payload = getPayload(token);
        if (payload == null) {
            return null;
        }
        if (Objects.isNull(keyValueStore.get(getKey(payload)))) {
            return null;
        }
        return INSTANCE.getUserByJwtPayload(payload);
    }

    @Override
    public String reFlushToken(String token, Date expirationDate) throws TokenExpirationException {
        JwtPayload jwtPayload = getPayload(token);
        Object user = INSTANCE.getUserByJwtPayload(getPayload(token));
        if(Objects.isNull(user)){
            throw new TokenExpirationException(token);
        }
        String newToken = generateToken(jwtPayload, expirationDate);
        keyValueStore.set(getKey(jwtPayload), newToken);
        return newToken;
    }

    @Override
    public String generateToken(@NonNull JwtPayload jwtPayload, @NonNull Serializable userPrincipal){
        String token = payloadToToken(jwtPayload);
        keyValueStore.set(getKey(jwtPayload), true);
        return token;
    }

    /**
     * 获取存储系统中的key
     * 不限制用户token个数
     * @param jwtPayload
     * @return
     */
    private CacheKey getKey(JwtPayload jwtPayload){
        //        每个用户可以存几个key? 同一个用户每个登陆设备只能有一个key
        String key = StrUtil.format("{}-{}", jwtPayload.getUserId(), jwtPayload.getLoginDevice().name());
        //        不限制用户token个数
        //        return StrUtil.format("{}-{}", jwtPayload.getUserId(), jwtPayload.getLoginTime().getTime());
        return CacheKey.of(() -> "token:", key);

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        repositories = new Repositories(applicationContext);
        defaultRepositoryInvokerFactory = new DefaultRepositoryInvokerFactory(repositories, DefaultConversionService.getSharedInstance());
    }

    @Override
    public void afterSingletonsInstantiated() {
        INSTANCE = SpringUtil.getBean(JwtTokenService.class);
        String sec = SystemConfigKeys.JWT_SECRET_KEY.getValue();
        this.secret = sec.getBytes();
    }
}

