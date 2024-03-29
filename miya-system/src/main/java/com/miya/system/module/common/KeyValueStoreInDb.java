package com.miya.system.module.common;

import cn.hutool.json.JSONUtil;
import com.miya.common.module.cache.CacheKey;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.system.module.common.po.QSysCache;
import com.miya.system.module.common.po.SysCache;
import com.miya.system.module.common.repository.SysCacheRepository;
import com.querydsl.core.types.ExpressionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;


/**
 * 基于数据库的kv存储器
 */
@Slf4j
@RequiredArgsConstructor
@ManagedResource
@ConditionalOnMissingBean(value = {KeyValueStore.class})
public class KeyValueStoreInDb implements KeyValueStore {

    private final SysCacheRepository sysCacheRepository;

    /**
     * 获取配置项
     * @param key
     */
    @ManagedOperation
    public String get(String key) {
        QSysCache qSysCache = QSysCache.sysCache;
        Optional<SysCache> one = sysCacheRepository.findOne(
                ExpressionUtils.and(
                        qSysCache.key.eq(key),
                        ExpressionUtils.or(qSysCache.expireDate.after(new Date()), qSysCache.expireDate.isNull())
                ));
        return one.map(SysCache::getVal).orElse(null);
    }

    /**
     * 获取配置项
     * @param key
     */
    public <T> T get(String key, Class<T> tClass) {
        QSysCache qSysCache = QSysCache.sysCache;
        Optional<SysCache> one = sysCacheRepository.findOne(qSysCache.key.eq(key)
                .and(qSysCache.expireDate.isNull().or(qSysCache.expireDate.after(new Date()))));
        return one.map(sysCache -> JSONUtil.toBean(sysCache.getVal(), tClass)).orElse(null);
    }

    /**
     * 设置配置项
     * @param key
     */
    public void set(String key, Serializable value) {
        set(key, value, null);
    }

    /**
     * 设置配置项
     *
     * @param key
     * @param value
     * @param expirationDate 过期时间
     */
    @Transactional
    public void set(String key, Serializable value, Date expirationDate) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key不可为空");
        }
        if (value == null) {
            throw new RuntimeException("value不可为空");
        }
        Iterable<SysCache> all = sysCacheRepository.findAll(QSysCache.sysCache.key.eq(key));
        sysCacheRepository.deleteAllInBatch(all);
        //设置时统一json化
        SysCache sysCache = new SysCache(key, JSONUtil.toJsonStr(value), expirationDate);
        sysCacheRepository.save(sysCache);
    }

    /**
     * 刪除
     * @param key   存储key
     */
    @ManagedOperation
    public void remove(String key) {
        Iterable<SysCache> all = sysCacheRepository.findAll(QSysCache.sysCache.key.eq(key));
        sysCacheRepository.deleteAll(all);
    }

    /**
     * 真正删除已经逻辑删除的数据
     */
    @ManagedOperation
    @Transactional
    public void realDeleteExpirationCache() {
        sysCacheRepository.deleteAllByExpireDateBefore(new Date());
    }


    @Override
    public String get(CacheKey key) {
        return this.get(key.toKey());
    }

    @Override
    public <T> T get(CacheKey key, Class<T> tClass) {
        return this.get(key.toKey(), tClass);
    }

    @Override
    public void set(CacheKey key, Serializable value) {
        this.set(key.toKey(), value);
    }

    @Override
    public void set(CacheKey key, Serializable value, Date expirationDate) throws UnsupportedOperationException {
        this.set(key.toKey(), value, expirationDate);
    }

    @Override
    public void remove(CacheKey key) {
        this.remove(key.toKey());
    }
}
