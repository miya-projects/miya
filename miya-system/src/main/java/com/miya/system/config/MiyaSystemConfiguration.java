package com.miya.system.config;


import com.miya.common.config.orm.source.DataSourceConfigure;
import com.miya.system.module.FlagForMiyaSystemModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import java.util.List;

/**
 * miya配置
 */
@Configuration
@Import(cn.hutool.extra.spring.SpringUtil.class)
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
