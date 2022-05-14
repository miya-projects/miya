package com.miya.system.module.oss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.miya.common.module.config.SysConfigService;
import com.miya.system.module.oss.service.impl.AliyunSysFileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.Resource;
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

        Supplier<Optional<String>> ossDomain = SpringUtil.getBean(SysConfigService.class)
                .getSupplier(SysConfigService.SystemConfigKey.OSS_DOMAIN);

        String endpoint = aliyun.getEndpoint();
        if (ossDomain.get().isPresent()){
            String domain = ossDomain.get().get();
            if(StrUtil.isNotBlank(domain)){
                // 数据库的自定义域名配置优先级高于配置文件。
                log.info("阿里云OSS：使用自定义域名: {}", domain);
                endpoint = domain;
            }
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
