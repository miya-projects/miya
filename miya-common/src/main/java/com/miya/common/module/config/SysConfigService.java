package com.miya.common.module.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.init.SystemInit;
import com.miya.common.module.init.SystemInitErrorException;
import com.querydsl.core.BooleanBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 系统配置
 */
@Service
@Slf4j
@ManagedResource
@RequiredArgsConstructor
public class SysConfigService implements SystemInit {

    private final SysConfigRepository configRepository;
    private final ConversionService conversionService = DefaultConversionService.getSharedInstance();

    @Override
    public void init() throws SystemInitErrorException {
        Boolean isInitialize = get(SystemConfigKeys.IS_INITIALIZE);
        if (!isInitialize) {
            // 还未初始化，进行初始化
            for (SystemConfigKeys configKey : SystemConfigKeys.values()) {
                put(configKey.name(), configKey.getDefaultValue(), configKey.getName(), "SYSTEM");
            }
        } else {
            // 增量更新
            for (SystemConfigKeys configKey : SystemConfigKeys.values()) {
                touchSystemConfig(configKey);
            }
        }
    }

    /**
     * 摸一下系统配置，如果不存在就创建
     * @param systemConfig
     */
    public void touchSystemConfig(SystemConfig systemConfig) {
        Optional<String> val = get(systemConfig.name());
        if (!val.isPresent()) {
            put(systemConfig.name(), systemConfig.getDefaultValue(), systemConfig.getName(), "SYSTEM");
        }
    }


    /**
     *  注册lazymap bean，可通过以下方式注入配置，属性，支持动态重载配置
     *  <code>
     *     \@Value("#{sysConfig['SYSTEM_NAME']}") <br />
     *     private Supplier<String> systemName;
     *  </code>
     */
    @Bean(name = "sysConfig")
    public Map<String, Supplier<String>> sysConfig() {
        return MapUtils.lazyMap(new HashMap<>(),
                key -> () -> SpringUtil.getBean(SysConfigService.class).get(key).orElse("")
        );
    }

    /**
     * 获取所有配置，分组过滤
     * @param group 如果为空就获取所有分组
     */
    public List<SysConfig> configs(String group) {
        BooleanBuilder bb = new BooleanBuilder();
        if (group != null) {
            bb.and(QSysConfig.sysConfig.group.eq(group));
        }
        Iterable<SysConfig> all = configRepository.findAll(bb);
        return CollUtil.newArrayList(all);
    }

    /**
     * 系统元信息，第一次访问首页时加载
     */
    @AllArgsConstructor
    @Getter
    public static class SystemMeta {
        private final String systemName;
        private final String version;
        private final String exportWay;
    }

    /**
     * 获取系统元信息
     */
    public SystemMeta getSystemMeta() {
        return new SystemMeta(get(SystemConfigKeys.SYSTEM_NAME), get(SystemConfigKeys.SYSTEM_VERSION), get(SystemConfigKeys.EXPORT_WAY));
    }

    /**
     * 返回supplier包装过的参数，推荐使用，每次get都会重新加载参数(缓存或DB)。且低依赖(不用依赖于整个configService)。
     * @param key
     */
    public <T> Supplier<T> getSupplier(SystemConfigKeys key) {
        return () -> SpringUtil.getBean(SysConfigService.class).get(key);
    }

    @Cacheable(cacheNames = "SYS_CONFIG", key = "#key.name()")
    public <T> T get(SystemConfigKeys key) {
        return (T)get(key.name(), key.getValueType()).orElse(conversionService.convert(key.getDefaultValue(), key.getValueType()));
    }

    /**
     * 获取配置项
     * @param key
     */
    @ManagedOperation
    @Cacheable(cacheNames = "SYS_CONFIG", key = "#key")
    public Optional<String> get(String key) {
        Optional<SysConfig> config = configRepository.findOne(QSysConfig.sysConfig.key.eq(key));
        return config.map(SysConfig::getVal);
    }

    /**
     * 获取配置项
     * @param key
     */
    @ManagedOperation
    @Cacheable(cacheNames = "SYS_CONFIG", key = "#key")
    public <T> Optional<T> get(String key, Class<T> valueType) {
        Optional<SysConfig> config = configRepository.findOne(QSysConfig.sysConfig.key.eq(key));
        return config.map(SysConfig::getVal).map(value -> conversionService.convert(value, valueType));
    }

    /**
     * 设置配置项，必须数据库提前有该配置项
     * @param key
     * @param value
     */
    @ManagedOperation
    @CacheEvict(cacheNames = "SYS_CONFIG", key = "#key")
    public void set(String key, String value) {
        Optional<SysConfig> sysConfigOptional = configRepository.findOne(QSysConfig.sysConfig.key.eq(key));
        SysConfig sysConfig = sysConfigOptional.orElseThrow(() -> new RuntimeException(StrUtil.format("配置项【{}】不存在", key)));
        sysConfig.setVal(value);
        configRepository.save(sysConfig);
    }

    /**
     * 设置配置项，没有key就新建
     * @param key
     * @param value
     */
    @ManagedOperation
    @CacheEvict(cacheNames = "SYS_CONFIG", key = "#key")
    public void put(String key, String value, String desc, String group) {
        Optional<SysConfig> sysConfigOptional = configRepository.findOne(QSysConfig.sysConfig.key.eq(key));
        SysConfig sysConfig = sysConfigOptional.orElseGet(() -> {
            SysConfig config = new SysConfig();
            config.setKey(key);
            config.setGroup(group);
            config.setDesc(desc);
            return config;
        });
        sysConfig.setVal(value);
        configRepository.save(sysConfig);
    }

    @CacheEvict(cacheNames = "SYS_CONFIG", allEntries = true)
    public void cleanAllCache() {
        SpringUtil.getApplicationContext().publishEvent(new ReloadConfigEvent());
    }

    @PostConstruct
    public void afterPropertiesSet() {
        if (!get(SystemConfigKeys.BACKEND_DOMAIN.name()).isPresent()) {
            log.warn("未配置后端访问域名，将使用默认值{}", SystemConfigKeys.BACKEND_DOMAIN.getDefaultValue());
        }
    }
}
