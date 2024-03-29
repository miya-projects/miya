package com.miya.system.module.oss;

import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.config.SystemConfigKeys;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.module.oss.service.impl.BareSysFileService;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 关于oss的配置
 */
@Configuration
@EnableConfigurationProperties(OssConfigProperties.class)
public class OssConfig {

    @Resource
    private SysFileRepository sysFileRepository;
    @Resource
    private OssConfigProperties ossConfigProperties;
    @Resource
    private SysConfigService configService;

    /**
     * 默认使用BareSysFileService
     */
    @Bean
    @ConditionalOnMissingBean(SysFileService.class)
    public BareSysFileService bareSysFileService(){
        return new BareSysFileService(sysFileRepository, ossConfigProperties.getBare(), configService.getSupplier(SystemConfigKeys.BACKEND_DOMAIN));
    }

    /**
     * 默认的OSS配置
     */
    @Bean
    @ConditionalOnMissingBean(MiyaSystemOssConfig.class)
    public MiyaSystemOssConfig defaultConfig(){
        return new MiyaSystemOssConfig(){};
    }
}
