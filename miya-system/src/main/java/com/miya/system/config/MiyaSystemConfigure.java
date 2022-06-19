package com.miya.system.config;

import com.miya.system.config.web.ReadableEnum;

import java.util.List;

/**
 * 实现该接口进行模块的自定义
 */
public interface MiyaSystemConfigure {

    /**
     * 增加扫描ReadableEnum的包
     * {@link ReadableEnum  }
     * @param scanPackages
     */
    void addScanPackageForReadableEnum(List<String> scanPackages);

}
