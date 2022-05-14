package com.miya.common.config.orm.source;

import lombok.Getter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据源配置组合 多个模块中的配置或扫描需要在多个分散类里配置，然后在这里合并
 */
@Component
public class DataSourceConfig {

    @Getter
    private final List<Class<?>> classes = new ArrayList<>();

    public DataSourceConfig(List<DataSourceConfigure> configures) {
        for (DataSourceConfigure configure : configures) {
            configure.addOrmPackages(classes);
        }
    }

}
