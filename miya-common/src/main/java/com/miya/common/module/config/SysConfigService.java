package com.miya.common.module.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.init.SystemInit;
import com.miya.common.module.init.SystemInitErrorException;
import com.querydsl.core.BooleanBuilder;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.util.CastUtils;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 系统配置
 */
@Service("sysConfigService")
@Slf4j
@ManagedResource
@RequiredArgsConstructor
public class SysConfigService implements SystemInit, SmartInitializingSingleton {

    private final SysConfigRepository configRepository;
    private final ConversionService conversionService = DefaultConversionService.getSharedInstance();

    @Override
    public void init() throws SystemInitErrorException {
        Boolean isInitialize = getValOrDefaultVal(SystemConfigKeys.IS_INITIALIZE);
        if (!isInitialize) {
            // 还未初始化，进行初始化
            for (SystemConfigKeys configKey : SystemConfigKeys.values()) {
                put(configKey.name(), configKey.getDefaultValue(), configKey.getName(), configKey.group());
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
     */
    @CacheEvict(cacheNames = "SYS_CONFIG", key = "#systemConfig.group() + #systemConfig.name()")
    public void touchSystemConfig(SystemConfig systemConfig) {
        Optional<?> val = get(systemConfig);
        if (val.isEmpty()) {
            put(systemConfig.name(), systemConfig.getDefaultValue(), systemConfig.getName(), systemConfig.group());
        }
    }

    /**
     * 获取所有配置，分组过滤
     * @param group 如果为空就获取所有分组
     */
    public List<SysConfig> getConfigsByGroup(String group) {
        BooleanBuilder bb = new BooleanBuilder();
        if (group != null) {
            bb.and(QSysConfig.sysConfig.group.eq(group));
        }
        Iterable<SysConfig> all = configRepository.findAll(bb);
        return CollUtil.newArrayList(all);
    }

    /**
     * 获取系统元信息
     */
    public SystemMeta getSystemMeta() {
        return new SystemMeta(getValOrDefaultVal(SystemConfigKeys.SYSTEM_NAME), getValOrDefaultVal(SystemConfigKeys.SYSTEM_VERSION),
                getValOrDefaultVal(SystemConfigKeys.EXPORT_WAY));
    }

    /**
     * 返回supplier包装过的参数，推荐使用，每次get都会重新加载参数(缓存或DB)。且低依赖(不用依赖于整个configService)。
     * @param systemConfig
     */
    @Cacheable(cacheNames = "SYS_CONFIG", key = "#systemConfig.group() + #systemConfig.name()")
    public <T> Supplier<T> getSupplier(SystemConfig systemConfig) {
        return () -> getValOrDefaultVal(systemConfig);
    }

    /**
     * 返回supplier包装过的参数，推荐使用，每次get都会重新加载参数(缓存或DB)。且低依赖(不用依赖于整个configService)。
     */
    @Cacheable(cacheNames = "SYS_CONFIG", key = "#group + #key")
    public <T> Supplier<T> getSupplier(String group, String key, Class<T> valueType) {
        return () -> get(group, key, valueType);
    }

    @Cacheable(cacheNames = "SYS_CONFIG", key = "#systemConfig.group() + #systemConfig.name()")
    public <T> T getValOrDefaultVal(SystemConfig systemConfig) {
        Object val = get(systemConfig.group(), systemConfig.name(), systemConfig.getValueType());
        if (val != null) {
            return CastUtils.cast(val);
        }
        return CastUtils.cast(Objects.requireNonNull(conversionService.convert(systemConfig.getDefaultValue(), systemConfig.getValueType())));
    }

    @Cacheable(cacheNames = "SYS_CONFIG", key = "#systemConfig.group() + #systemConfig.name()")
    public <T> Optional<T> get(SystemConfig systemConfig) {
        return Optional.ofNullable((T)get(systemConfig.group(), systemConfig.name(), systemConfig.getValueType()));
    }

    /**
     * 获取配置项
     * @param key
     */
    @ManagedOperation
    @Nullable
    @Cacheable(cacheNames = "SYS_CONFIG", key = "#group + #key")
    public <T> T get(String group, String key, Class<T> valueType) {
        QSysConfig qSysConfig = QSysConfig.sysConfig;
        Optional<SysConfig> config = configRepository.findOne(qSysConfig.group.eq(group).and(qSysConfig.key.eq(key)));
        return config.map(SysConfig::getVal).map(value -> conversionService.convert(value, valueType)).orElse(null);
    }

    /**
     * 设置配置项，必须数据库提前有该配置项
     * @param key
     * @param value
     */
    @ManagedOperation
    @CacheEvict(cacheNames = "SYS_CONFIG", key = "#group + #key")
    public void set(String group, String key, String value) {
        QSysConfig qSysConfig = QSysConfig.sysConfig;
        Optional<SysConfig> sysConfigOptional = configRepository.findOne(qSysConfig.group.eq(group).and(qSysConfig.key.eq(key)));
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
    @CacheEvict(cacheNames = "SYS_CONFIG", key = "#group + #key")
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

    @Override
    public void afterSingletonsInstantiated() {
        if (StrUtil.isBlank(getValOrDefaultVal(SystemConfigKeys.BACKEND_DOMAIN))) {
            log.warn("未配置后端访问域名，将使用默认值{}", SystemConfigKeys.BACKEND_DOMAIN.getDefaultValue());
        }
    }
}
