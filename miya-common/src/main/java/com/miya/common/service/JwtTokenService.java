package com.miya.common.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miya.common.config.web.jwt.JwtPayload;
import com.miya.common.config.web.jwt.TokenExpirationException;
import com.miya.common.config.web.jwt.TokenService;
import com.miya.common.module.cache.CacheKey;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.common.module.config.SysConfig;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.init.SystemInit;
import com.miya.common.module.init.SystemInitErrorException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.*;

/**
 * @author 杨超辉
 */
@Slf4j
public class JwtTokenService implements Serializable, SystemInit, TokenService, ApplicationContextAware {
    private static final long serialVersionUID = -3301605591108950415L;
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_ID = "id";
    private static final String CLAIM_KEY_CREATED = "createdDate";
    private static final String CLAIM_KEY_AUTH_TYPE = "authType";
    private static final String CLAIM_KEY_LOGIN_DEVICE = "loginDevice";
    private static final String JWT_SECRET_KEY = "JWT_SECRET";

    /**
     * 签名密钥
     */
    private byte[] secret;

    private final SysConfigService configService;
    private final KeyValueStore keyValueStore;



    @Override
    public void init() throws SystemInitErrorException {
        // 重置secret
        // 随机生成一个Secret
        String secStr = RandomUtil.randomString(18);
        this.configService.put(JWT_SECRET_KEY, secStr, "jwt密钥(务必不可泄露)", SysConfig.GROUP_SYSTEM);
        this.secret = secStr.getBytes();
    }

    public JwtTokenService(SysConfigService configService, KeyValueStore keyValueStore){
        this.configService = configService;
        this.keyValueStore = keyValueStore;
        String sec = configService.get(JWT_SECRET_KEY).orElseGet(() -> {
            log.info("未设置JWT密钥，生成一个");
            String secStr = RandomUtil.randomString(18);
            this.configService.put(JWT_SECRET_KEY, secStr, "jwt密钥(务必不可泄露)", SysConfig.GROUP_SYSTEM);
            return secStr;
        });
        this.secret = sec.getBytes();
    }


    /**
     * 获取payload 如token不合法，返回null
     * @param token
     */
    public JwtPayload getPayload(String token) {
        Claims claims = getClaimsFromToken(token);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(JSONUtil.toJsonStr(claims), JwtPayload.class);
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("token 不合法: {}", token));
        }
    }

    /**
     * 获取token的创建时间
     * @param token
     */
    public Date getCreatedDateFromToken(String token) {
        Date created;
        try {
            final Claims claims = getClaimsFromToken(token);
            created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
        } catch (Exception e) {
            created = null;
        }
        return created;
    }

    /**
     * 获取token中的过期时间
     * @param token
     */
    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    /**
     * 获取token中存的信息 payload
     * @param token
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.trace(ExceptionUtils.getStackTrace(e));
            claims = null;
        }
        return claims;
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
     * @param claims    token payload
     * @param expirationDate    token过期时间
     */
    private String generateToken(Map<String, Object> claims, Date expirationDate) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成token
     * @param jwtPayload    token payload
     * @param expirationDate    token过期时间
     */
    public String generateToken(JwtPayload jwtPayload, Date expirationDate) {
        return Jwts.builder()
                .setClaims(BeanUtil.beanToMap(jwtPayload))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 刷新token
     * @param token
     * @param expirationDate 过期日期
     */
    public String refreshToken(String token, Date expirationDate) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = generateToken(claims, expirationDate);
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
        RepositoryInvoker invokerFor = defaultRepositoryInvokerFactory.getInvokerFor(jwtPayload.getUserClass());
        Optional<Object> userOptional = invokerFor.invokeFindById(jwtPayload.getUserId());
        Object user = userOptional.orElse(null);
        if (user != null) {
            // 这里把懒加载的属性初始化一下，注入后可以直接使用，偷懒的做法，还有一种办法是不在service以外的地方使用PO，进行一次对象转换
            Collection<PropDesc> props = BeanUtil.getBeanDesc(user.getClass()).getProps();
            for (PropDesc prop : props) {
                Class<?> type = prop.getField().getType();
                if (type.isAssignableFrom(Set.class) || type.isAssignableFrom(Collection.class)) {
                    Set set = ReflectUtil.invoke(user,  prop.getGetter());
                    Hibernate.initialize(set);
                }
            }
        }
        return user;
    }

    @Override
    public Object getUserByToken(String token){
        JwtPayload payload = getPayload(token);
        if (Objects.isNull(keyValueStore.get(getKey(payload)))){
            throw new BadCredentialsException("token不合法哦");
        }
        return SpringUtil.getBean(JwtTokenService.class).getUserByJwtPayload(payload);
    }

    @Override
    public String reFlushToken(String token, Date expirationDate) throws TokenExpirationException {
        JwtPayload jwtPayload = getPayload(token);
        Object user = SpringUtil.getBean(JwtTokenService.class).getUserByJwtPayload(getPayload(token));
        if(Objects.isNull(user)){
            throw new TokenExpirationException(token);
        }
        String newToken = generateToken(jwtPayload, expirationDate);
        keyValueStore.set(getKey(jwtPayload), newToken);
        return newToken;
    }

    @Override
    public String generateToken(@NonNull JwtPayload jwtPayload, @NonNull Serializable user, Date expirationDate){
        String token = generateToken(jwtPayload, expirationDate);
        CacheKey key = getKey(jwtPayload);
        keyValueStore.set(key, user);
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
}

