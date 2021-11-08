package com.miya.system.config.orm;

import com.miya.common.module.base.ExtendsRepositoryImpl;
import com.miya.system.module.FlagForMiyaSystemModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.ArrayList;
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
        basePackageClasses = {FlagForMiyaSystemModule.class},
        bootstrapMode = BootstrapMode.DEFERRED,
        repositoryBaseClass = ExtendsRepositoryImpl.class
)
public class PrimaryDataSourceConfig {

    @Bean("ormPackages")
    public List<Class<?>> ormPackages(){
        ArrayList<Class<?>> classes = new ArrayList<>();
        classes.add(FlagForMiyaSystemModule.class);
        return classes;
    }

}
