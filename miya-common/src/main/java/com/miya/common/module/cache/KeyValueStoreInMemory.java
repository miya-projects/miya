package com.miya.common.module.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的kv存储器
 */
@Slf4j
public class KeyValueStoreInMemory implements KeyValueStore {

    private final ConcurrentHashMap<Serializable, Object> session = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Serializable, TimerTask> timerMap = new ConcurrentHashMap<>();
    private final Timer timer = new Timer();

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


    public String get(String key) {
        return Optional.ofNullable(session.get(key)).map(Object::toString).orElse(null);
    }

    public <T> T get(String key, Class<T> tClass) {
        return CastUtils.cast(Optional.ofNullable(session.get(key)).orElse(null));
    }

    public void set(String key, Serializable value) {
        session.put(key, value);
        Optional.ofNullable(timerMap.get(key)).ifPresent(TimerTask::cancel);
    }

    public void set(String key, Serializable value, Date expirationDate) throws UnsupportedOperationException {
        session.put(key, value);
        //在key过期时删除该key
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                session.remove(key);
            }
        };
        timer.schedule(timerTask, expirationDate);
        timerMap.put(key, timerTask);
    }

    public void remove(String key) {
        session.remove(key);
    }
}
