package com.miya.system.module.oss.domain;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.config.SystemConfigKeys;
import com.miya.system.module.oss.model.SysFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

/**
 * 程序自带的公开静态文件，提供外部访问
 * 一般程序硬编码，不做持久化
 */
@Slf4j
public class PublicAssetsSysFile extends SysFile {

    private static Supplier<String> BACKEND_DOMAIN_SUPPLIER = () -> {
        Supplier<String> supplier = SpringUtil.getBean(SysConfigService.class).getSupplier(SystemConfigKeys.BACKEND_DOMAIN);
        BACKEND_DOMAIN_SUPPLIER = supplier;
        return supplier.get();
    };

    public PublicAssetsSysFile(String path){
        this(path, FileUtil.mainName(path));
    }

    public PublicAssetsSysFile(String path, String name){
        this.setPath(path);
        this.setFilename(name);
    }

    @Override
    public String getUrl() {
        String backendDomain = BACKEND_DOMAIN_SUPPLIER.get();
        try {
            return new URL(new URL(backendDomain), getPath()).toString();
        } catch (MalformedURLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return backendDomain;
        }
    }
}
