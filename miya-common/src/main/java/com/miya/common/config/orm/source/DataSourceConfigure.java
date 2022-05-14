package com.miya.common.config.orm.source;

import java.util.List;

/**
 * 配置数据源
 */
public interface DataSourceConfigure {

    /**
     * 增加被扫描的包
     */
    default void addOrmPackages(List<Class<?>> ormPackages){};

}
