package com.miya.system.module.oss;

import com.miya.common.module.config.SysConfigService;
import com.miya.system.module.oss.service.impl.AliyunSysFileService;
import com.miya.system.module.oss.service.impl.BareSysFileService;
import com.miya.system.module.oss.service.impl.MinioSysFileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.Resource;

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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "bare")
    public BareSysFileService bareSysFileService(){
        return new BareSysFileService(sysFileRepository, ossConfigProperties.getBare(), configService.getBackendDomain());
    }

    @Bean
    @ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "minio")
    public MinioSysFileService minioSysFileService(){
        return new MinioSysFileService(sysFileRepository, ossConfigProperties.getMinio());
    }

    @Bean
    @ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "aliyun")
    public AliyunSysFileService aliyunSysFileService(){
        return new AliyunSysFileService(sysFileRepository, ossConfigProperties.getAliyun());
    }

}
