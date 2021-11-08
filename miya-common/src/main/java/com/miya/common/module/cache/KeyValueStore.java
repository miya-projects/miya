package com.miya.common.module.cache;

import java.io.Serializable;
import java.util.Date;

/**
 * key-value形式的存储器
 */
public interface KeyValueStore {

    /**
     * 获取配置项 没有的话返回null
     * @param key
     * @return
     */
    String get(CacheKey key);

    /**
     * 获取配置项
     * @param key
     * @return
     */
    <T> T get(CacheKey key, Class<T> tClass);

    /**
     * 设置数据
     * @param key
     * @return
     */
    void set(CacheKey key, Serializable value);

    /**
     * 设置数据
     * @param key
     * @param value
     * @param expirationDate 过期时间
     */
    void set(CacheKey key, Serializable value, Date expirationDate) throws UnsupportedOperationException;

    /**
     * 刪除
     * @param key
     */
    void remove(CacheKey key);

}
