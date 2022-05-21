package com.miya.system.config;

import com.miya.system.config.web.ReadableEnum;

import java.util.List;

public interface MiyaSystemConfigure {

    /**
     * 增加扫描ReadableEnum的包
     * {@link ReadableEnum  }
     * @param scanPackages
     */
    void addScanPackageForReadableEnum(List<String> scanPackages);

}
