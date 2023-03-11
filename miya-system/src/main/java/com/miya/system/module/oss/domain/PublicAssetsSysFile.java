package com.miya.system.module.oss.domain;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.system.module.oss.model.SysFile;
import com.miya.common.module.config.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 程序自带的公开静态文件，提供外部访问
 * 一般程序硬编码，不做持久化
 */
@Slf4j
public class PublicAssetsSysFile extends SysFile {

    private Supplier<String> backendDomainSupplier;
    public PublicAssetsSysFile(String path){
        this(path, FileUtil.mainName(path));
    }

    public PublicAssetsSysFile(String path, String name){
        this.setPath(path);
        this.setFilename(name);
    }

    @Override
    public String getUrl() {
        if (backendDomainSupplier == null){
            backendDomainSupplier = SpringUtil.getBean(SysConfigService.class).getSupplier(SysConfigService.SystemConfigKey.BACKEND_DOMAIN);
        }
        String backendDomain = backendDomainSupplier.get();
        try {
            return new URL(new URL(backendDomain), getPath()).toString();
        } catch (MalformedURLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return backendDomain;
        }
    }
}
