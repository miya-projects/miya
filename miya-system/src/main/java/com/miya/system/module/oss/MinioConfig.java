package com.miya.system.module.oss;

import com.miya.system.module.oss.service.impl.MinioSysFileService;
import io.minio.MinioClient;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "minio")
public class MinioConfig {

    @Resource
    private SysFileRepository sysFileRepository;

    @Resource
    private OssConfigProperties ossConfigProperties;

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(MinioClient.class)
    @ConditionalOnProperty(prefix = "config.oss.minio", name = {"endpoint", "accessKey", "secretKey"})
    public MinioClient minioClient(){
        OssConfigProperties.Minio minio = ossConfigProperties.getMinio();
        return MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "config.oss", name = "type", havingValue = "minio")
    public MinioSysFileService minioSysFileService(MinioClient minioClient){
        OssConfigProperties.Minio minio = ossConfigProperties.getMinio();
        return new MinioSysFileService(sysFileRepository, minio.getBucketName(), minioClient);
    }

}
