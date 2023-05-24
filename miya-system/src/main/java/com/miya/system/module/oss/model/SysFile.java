package com.miya.system.module.oss.model;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.config.SysConfigService;
import com.miya.system.module.oss.service.SysFileService;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author 杨超辉
 * 对象存储实体
 * 抽象一般数据存储实体，不管在哪里存储一定有以下几项信息
 * 1. 在服务器的唯一标识，可以是相对路径、对象名
 * 2. 对象大小
 * 3. 能够提供对外访问的url
 * 4. 前端上传的文件名
 * 5. 其他元数据
 */
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(indexes = {@Index(name = "path", columnList = "path")})
public class SysFile extends BaseEntity {
    /**
     * 服务端全路径/或oss的object name
     */
    @Column(length = 100, nullable = false)
    private String path;

    /**
     * 前端上传的文件名
     */
    @Column(length = 100)
    private String filename;

    /**
     * 文件可读大小
     */
    @Column(length = 20, nullable = false)
    private String simpleSize;
    /**
     * 文件大小
     */
    @Column(length = 20, nullable = false)
    private Long size;

    /**
     * 分类
     */
    @Column(length = 50)
    private String category;

    @Type(JsonType.class)
    @Column(name = "extra", columnDefinition = "json")
    private Map<String, Object> extra;

    /**
     * @return 能够提供对外访问的url
     */
    public String getUrl(){
        // SysConfigService configService = SpringUtil.getBean(SysConfigService.class);
        // Optional<String> domainOptional = configService.get(SysConfigService.SystemConfigKey.OSS_DOMAIN);
        // if (domainOptional.isPresent()){
        //     String domain = domainOptional.get();
        //     if(StrUtil.isNotBlank(domain)){
        //         return domain + this.getPath();
        //     }
        // }
        return SpringUtil.getBean(SysFileService.class).getUrl(this);
    }

    /**
     * @return 兼容ng-alain默认name
     */
    public String getName(){
        return this.filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysFile sysFile = (SysFile) o;

        return Objects.equals(id, sysFile.id);
    }

    @Override
    public int hashCode() {
        return 216295067;
    }
}

