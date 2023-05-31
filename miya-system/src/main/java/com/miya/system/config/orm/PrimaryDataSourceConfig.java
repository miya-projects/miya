package com.miya.system.config.orm;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceWrapper;
import com.miya.common.config.orm.source.DataSourceConfig;
import com.miya.common.module.base.ExtendsRepositoryImpl;
import com.miya.system.module.FlagForMiyaSystemModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.configuration.EnversSettings;
import org.hibernate.envers.strategy.internal.ValidityAuditStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

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

    @Resource
    private DataSourceConfig dataSourceConfig;

    @Resource
    private JpaProperties jr;

    @Bean(initMethod = "init")
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    public DruidDataSource dataSource() {
        return new DruidDataSourceWrapper();
    }

    @SneakyThrows(SQLException.class)
    @Primary
    @Bean(name = "entityManagerFactory")
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(ObjectProvider<PersistenceUnitManager> persistenceUnitManager, DataSource dataSource) {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        String category = null;
        try (Connection connection = dataSource.getConnection()){
            category = connection.getCatalog() + "-" + "deleted";
        }

        Map<String, String> properties = jr.getProperties();
        properties.putIfAbsent("hibernate.integration.envers.enabled", Boolean.FALSE.toString());
        properties.putIfAbsent(EnversSettings.AUDIT_TABLE_SUFFIX, "");
        properties.putIfAbsent(EnversSettings.AUDIT_TABLE_PREFIX, "audit_");
        properties.putIfAbsent(EnversSettings.STORE_DATA_AT_DELETE, Boolean.TRUE.toString());
        properties.putIfAbsent(EnversSettings.DEFAULT_CATALOG, category);
        properties.putIfAbsent(EnversSettings.AUDIT_STRATEGY, ValidityAuditStrategy.class.getName());

//        properties.put("hibernate.envers.autoRegisterListeners", Boolean.FALSE.toString());

        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(adapter,
                jr.getProperties(), persistenceUnitManager.getIfAvailable());
        return builder
                .dataSource(dataSource)
                .packages(dataSourceConfig.getClasses().toArray(new Class[0]))
                .properties(jr.getProperties())
                .persistenceUnit("persistenceUnit")
                .build();
    }

    /**
     * 配置事物管理器
     */
    @Bean(name = "transactionManager")
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

}
