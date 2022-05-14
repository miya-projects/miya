package com.miya.common.config.orm.source;

import com.miya.common.config.orm.ExtensionMySQLDialect;
import com.miya.common.module.FlagForMiyaCommonModule;
import com.miya.common.module.base.ExtendsRepositoryImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.FlushMode;
import org.hibernate.cache.ehcache.internal.EhcacheRegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.annotation.Resource;
import javax.persistence.SharedCacheMode;
import java.util.*;

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
