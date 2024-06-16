package com.miya.system.module.oss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.config.SystemConfigKeys;
import com.miya.system.module.oss.service.impl.AliyunSysFileService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "aliyun")
public class AliyunOssConfig {

    @Resource
    private SysFileRepository sysFileRepository;

    @Resource
    private OssConfigProperties ossConfigProperties;

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(OSS.class)
    @ConditionalOnProperty(prefix = "config.oss.aliyun", name = {"access-key", "secret-key"})
    public OSS ossClient() {
        OssConfigProperties.Aliyun aliyun = ossConfigProperties.getAliyun();

        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);

        Optional<String> domainOptional = SpringUtil.getBean(SysConfigService.class)
                .get(SystemConfigKeys.OSS_DOMAIN);
        String endpoint = aliyun.getEndpoint();
        if(domainOptional.isPresent() && StrUtil.isNotBlank(domainOptional.get())){
            // 数据库的自定义域名配置优先级高于配置文件。
            log.info("阿里云OSS：使用自定义域名: {}", domainOptional.get());
            endpoint = domainOptional.get();
        }
        return new OSSClientBuilder().build(endpoint, aliyun.getAccessKey(), aliyun.getSecretKey(), conf);
    }

    @Bean
    @ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "aliyun")
    public AliyunSysFileService aliyunSysFileService(OSS ossClient) {
        OssConfigProperties.Aliyun aliyun = ossConfigProperties.getAliyun();
        return new AliyunSysFileService(sysFileRepository, aliyun.getBucketName(), ossClient);
    }

}
