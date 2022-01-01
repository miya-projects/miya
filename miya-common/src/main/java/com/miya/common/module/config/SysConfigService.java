package com.miya.common.module.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.miya.common.module.init.SystemInit;
import com.miya.common.module.init.SystemInitErrorException;
import com.querydsl.core.BooleanBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 系统配置
 */
@Service
@Slf4j
@ManagedResource
@RequiredArgsConstructor
public class SysConfigService implements InitializingBean, SystemInit {

    private final SysConfigRepository configRepository;

    @Value("${server.port}")
    private int port;

    @Override
    public void init() throws SystemInitErrorException {
        Optional<String> isInitialize = get(SystemConfigKey.IS_INITIALIZE);
        if (!isInitialize.isPresent()) {
            // 还未初始化，进行初始化
            put(SystemConfigKey.IS_INITIALIZE.name(), "1", SystemConfigKey.IS_INITIALIZE.name, "SYSTEM");
            put(SystemConfigKey.SYSTEM_NAME.name(), "MIYA", SystemConfigKey.SYSTEM_NAME.name, "SYSTEM");
            put(SystemConfigKey.SYSTEM_VERSION.name(), "1.0", SystemConfigKey.SYSTEM_VERSION.name, "SYSTEM");
            put(SystemConfigKey.BACKEND_DOMAIN.name(), "http://localhost:8080", SystemConfigKey.BACKEND_DOMAIN.name, "SYSTEM");
            put(SystemConfigKey.OSS_DOMAIN.name(), "", SystemConfigKey.OSS_DOMAIN.name, "SYSTEM");
        }
    }

    @AllArgsConstructor
    @Getter
    public enum SystemConfigKey implements Serializable {
        IS_INITIALIZE("是否初始化完毕"),
        SYSTEM_NAME("系统名称"),
        SYSTEM_VERSION("系统版本"),
        BACKEND_DOMAIN("后端域名(让后端知道怎么可以访问到自己)"),
        OSS_DOMAIN("OSS域名，设置后文件访问将直接使用该域名，需自行配置OSS后端");

        private final String name;

        public Object getValue() {
            SysConfigService configService = SpringUtil.getBean(SysConfigService.class);
            //不想给默认值 -> 应当有一次初始化
            return configService.get(this);
        }
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
    }

    /**
     * 获取系统元信息
     */
    public SystemMeta getSystemMeta() {
        return new SystemMeta(getSystemName(), get("SYSTEM_VERSION").orElse("0.0.1"));
    }

    /**
     * 获取系统名称
     */
    public String getSystemName() {
        return get(SystemConfigKey.SYSTEM_NAME).orElse("MiYa");
    }

    /**
     * 获取后端可访问前缀
     * eg: https://www.website.com/sdf
     */
    public String getBackendDomain() {
        return get(SystemConfigKey.BACKEND_DOMAIN).orElse(getDefaultDomain());
    }

    /**
     * 获取默认后端域名
     */
    private String getDefaultDomain() {
        return "http://localhost:" + this.port;
    }

    public Optional<String> get(SystemConfigKey key) {
        return get(key.name());
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

    @Override
    public void afterPropertiesSet() {
        if (!get(SystemConfigKey.BACKEND_DOMAIN).isPresent()) {
            log.warn("未配置后端访问域名，将使用默认值{}", getDefaultDomain());
        }
    }
}
