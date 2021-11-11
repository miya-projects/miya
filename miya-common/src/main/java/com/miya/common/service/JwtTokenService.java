package com.miya.common.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.security.authentication.BadCredentialsException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author 杨超辉
 */
@Slf4j
public class JwtTokenService implements Serializable, SystemInit, TokenService {
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
    private final EntityManager entityManager;



    @Override
    public void init() throws SystemInitErrorException {
        // 重置secret
        // 随机生成一个Secret
        String secStr = RandomUtil.randomString(18);
        this.configService.put(JWT_SECRET_KEY, secStr, "jwt密钥(务必不可泄露)", SysConfig.GROUP_SYSTEM);
        this.secret = secStr.getBytes();
    }

    public JwtTokenService(SysConfigService configService, KeyValueStore keyValueStore, EntityManager entityManager){
        this.configService = configService;
        this.keyValueStore = keyValueStore;
        this.entityManager = entityManager;
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }


    /**
     * 生成token
     * @param claims    token payload
     * @param expirationDate    token过期时间
     * @return
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
     * @return
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
     * @return
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



    /**
     * 根据jwtPayload获取用户对象
     * @param jwtPayload
     * @return
     */
    private Object getUserByJwtPayload(JwtPayload jwtPayload){
        String jpql = "from " + jwtPayload.getUserClass().getName() + " where id = :id";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("id", jwtPayload.getUserId());
        return query.getSingleResult();
    }

    @Override
    public Object getUserByToken(String token){
        JwtPayload payload = getPayload(token);
        if (Objects.isNull(keyValueStore.get(getKey(payload)))){
            throw new BadCredentialsException("token不合法哦");
        }
        return getUserByJwtPayload(payload);
    }

    @Override
    public String reFlushToken(String token, Date expirationDate) throws TokenExpirationException {
        JwtPayload jwtPayload = getPayload(token);
        Object user = getUserByJwtPayload(getPayload(token));
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
}

