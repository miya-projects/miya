package com.miya.system.module.oss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * oss配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "config.oss")
public class OssConfigProperties {
    private OssType type;
    @NestedConfigurationProperty
    private Minio minio;
    @NestedConfigurationProperty
    private Aliyun aliyun;
    @NestedConfigurationProperty
    private Bare bare = new Bare();

    public enum OssType {
        Bare,
        Minio,
        Aliyun
    }

    @Getter
    @Setter
    static public class Bare {
        /**
         * 上传绝对路径
         */
        private String uploadAbsolutePath = "/upload";
        /**
         * 可访问到文件的路径
         * 最终文件的访问路径为: backendDomain(系统参数可配置) + pathPatterns + 文件path
         */
        private String[] pathPatterns = new String[]{"/upload/**"};;
    }

    @Getter
    @Setter
    static public class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName = "miya";
    }

    @Getter
    @Setter
    static public class Aliyun {
        private String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        private String accessKey;
        private String secretKey;
        private String bucketName = "miya";
    }
}
