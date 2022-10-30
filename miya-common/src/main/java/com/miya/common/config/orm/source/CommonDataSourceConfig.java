package com.miya.common.config.orm.source;

import com.miya.common.module.FlagForMiyaCommonModule;
import com.miya.common.module.base.ExtendsRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.List;

/**
 * 数据源配置
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackageClasses = {FlagForMiyaCommonModule.class},
        bootstrapMode = BootstrapMode.DEFAULT,
        repositoryBaseClass = ExtendsRepositoryImpl.class
)
public class CommonDataSourceConfig implements DataSourceConfigure {

    @Override
    public void addOrmPackages(List<Class<?>> ormPackages) {
        ormPackages.add(FlagForMiyaCommonModule.class);
    }

}
