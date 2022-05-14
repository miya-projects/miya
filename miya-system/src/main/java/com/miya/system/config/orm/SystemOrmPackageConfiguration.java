package com.miya.system.config.orm;


import com.miya.common.config.orm.source.DataSourceConfigure;
import com.miya.system.module.FlagForMiyaSystemModule;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * 扩展entity扫描包
 */
@Configuration
public class SystemOrmPackageConfiguration implements DataSourceConfigure {


    @Override
    public void addOrmPackages(List<Class<?>> ormPackages) {
        ormPackages.add(FlagForMiyaSystemModule.class);
    }


}
