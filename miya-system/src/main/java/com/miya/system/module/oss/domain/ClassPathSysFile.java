package com.miya.system.module.oss.domain;

import cn.hutool.core.io.FileUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.system.module.oss.model.SysFile;
import com.miya.common.module.config.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 类路径下的文件，提供外部访问
 * 一般程序硬编码，不做持久化
 */
@Slf4j
public class ClassPathSysFile extends SysFile {

    public ClassPathSysFile(String path){
        this(path, FileUtil.mainName(path));
    }

    public ClassPathSysFile(String path, String name){
        this.setPath(path);
        this.setFilename(name);
    }

    @Override
    public String getUrl() {
        String backendDomain = SpringUtil.getBean(SysConfigService.class).getBackendDomain();
        try {
            return new URL(new URL(backendDomain), getPath()).toString();
        } catch (MalformedURLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return backendDomain;
        }
    }
}
