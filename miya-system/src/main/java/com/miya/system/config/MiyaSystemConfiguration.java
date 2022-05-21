package com.miya.system.config;


import com.miya.common.config.orm.source.DataSourceConfigure;
import com.miya.system.module.FlagForMiyaSystemModule;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * miya配置
 */
@Configuration
public class MiyaSystemConfiguration implements DataSourceConfigure, MiyaSystemConfigure {


    @Override
    public void addOrmPackages(List<Class<?>> ormPackages) {
        ormPackages.add(FlagForMiyaSystemModule.class);
    }


    @Override
    public void addScanPackageForReadableEnum(List<String> scanPackages) {
        scanPackages.add(FlagForMiyaSystemModule.class.getPackage().getName());
    }
}
