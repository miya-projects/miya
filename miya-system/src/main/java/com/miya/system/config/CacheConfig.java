package com.miya.system.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * 使用该缓存的目的应当是为了减小db压力或加快访问速度
 */
@Configuration
public class CacheConfig implements CachingConfigurer {

    @Override
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(50000)
                .expireAfterWrite(60 * 10, TimeUnit.SECONDS));
        return cacheManager;
    }
}
