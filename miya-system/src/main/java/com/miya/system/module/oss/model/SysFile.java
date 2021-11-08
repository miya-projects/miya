package com.miya.system.module.oss.model;

import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.config.SysConfigService;
import com.miya.system.module.oss.service.SysFileService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
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
public class SysFile extends BaseEntity {
    /**
     * 服务端全路径/或oss的object name
     */
    private String path;
    /**
     * 前端上传的文件名
     */
    private String filename;
    /**
     * 文件可读大小
     */
    private String simpleSize;
    /**
     * 文件大小
     */
    private Long size;

    /**
     * 分类
     */
    private String category;

    @Type(type = "json")
    private Map<String, Object> extra;
    /**
     * 能够提供对外访问的url
     * @return
     */
    public String getUrl(){
        SysConfigService configService = SpringUtil.getBean(SysConfigService.class);
        Optional<String> domainOptional = configService.get(SysConfigService.SystemConfigKey.OSS_DOMAIN);
        if (domainOptional.isPresent()){
            String domain = domainOptional.get();
            return domain + this.getPath();
        }
        return SpringUtil.getBean(SysFileService.class).getUrl(this);
    }

    /**
     * 兼容ng-alain默认name
     * @return
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

