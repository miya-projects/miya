package com.miya.system.config;

import com.miya.common.config.MiyaCommonAutoConfiguration;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.service.JwtTokenService;
import com.miya.system.module.FlagForMiyaSystemModule;
import com.miya.system.module.common.repository.CacheRepository;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.system.module.common.KeyValueStoreInDb;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ComponentScan(
        basePackageClasses = {
                FlagForMiyaSystemModule.class,
                MiyaCommonAutoConfiguration.class,
                MiyaSystemAutoConfiguration.class
        }
)
@EnableCaching
@Configuration
public class MiyaSystemAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = {KeyValueStore.class})
    public KeyValueStore defaultKeyValueStore(CacheRepository cacheRepository) {
        return new KeyValueStoreInDb(cacheRepository);
    }

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * todo 注入了多个KeyValueStore应该如何选择？
     *
     * @param keyValueStore
     * @return
     */
    @Bean
    public JwtTokenService tokenStore(SysConfigService configService, KeyValueStore keyValueStore) {
        return new JwtTokenService(configService, keyValueStore, entityManager);
    }

    @Bean
    public KeyValueStore keyValueStore(CacheRepository repository){
        return new KeyValueStoreInDb(repository);
    }
}
