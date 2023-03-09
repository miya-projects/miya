package com.miya.system.config;

import com.miya.common.config.MiyaCommonAutoConfiguration;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.service.JwtTokenService;
import com.miya.system.module.FlagForMiyaSystemModule;
import com.miya.system.module.common.repository.CacheRepository;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.system.module.common.KeyValueStoreInDb;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@ComponentScan(
        basePackageClasses = {
                FlagForMiyaSystemModule.class,
                MiyaSystemAutoConfiguration.class
        }
)
@EnableCaching
@Configuration
@AutoConfigureAfter(MiyaCommonAutoConfiguration.class)
public class MiyaSystemAutoConfiguration {

    /**
     * 需扫描ReadableEnum枚举的包集合
     * @param miyaSystemConfigures
     */
    @Bean("scanPackageForReadableEnum")
    public List<String> scanPackageForReadableEnum(List<MiyaSystemConfigure> miyaSystemConfigures){
        List<String> readableEnumStr = new ArrayList<>();
        for (MiyaSystemConfigure configure : miyaSystemConfigures) {
            configure.addScanPackageForReadableEnum(readableEnumStr);
        }
        return readableEnumStr;
    }

    @Bean
    @ConditionalOnMissingBean(value = {KeyValueStore.class})
    public KeyValueStore defaultKeyValueStore(CacheRepository cacheRepository) {
        return new KeyValueStoreInDb(cacheRepository);
    }

    /**
     * todo 注入了多个KeyValueStore应该如何选择？
     *
     * @param keyValueStore
     */
    @Bean
    public JwtTokenService tokenStore(SysConfigService configService, KeyValueStore keyValueStore) {
        return new JwtTokenService(configService, keyValueStore);
    }

    // @Bean
    // public KeyValueStore keyValueStore(CacheRepository repository){
    //     return new KeyValueStoreInDb(repository);
    // }
}
