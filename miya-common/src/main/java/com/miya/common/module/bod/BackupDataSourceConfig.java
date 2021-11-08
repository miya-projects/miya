package com.miya.common.module.bod;

import com.alibaba.druid.pool.DruidDataSource;
import com.miya.common.module.FlagForMiyaCommonModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MySQL57Dialect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 备份数据源配置
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(value = "config.backup-on-delete.enable", havingValue = "true")
public class BackupDataSourceConfig implements InitializingBean {

    @Resource
    private DbConfig dbConfig;
    /**
     * 额外需要扫描的包，可通过该点进行entityManager扫描包扩展
     */
    @Resource
    @Qualifier("ormPackages")
    private Optional<List<Class<?>>> packages;

    Map<String, String> properties = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        properties.put(AvailableSettings.HBM2DDL_AUTO, "update");
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy.class.getName());
        properties.put(AvailableSettings.DIALECT, MySQL57Dialect.class.getName());
        // properties.put(AvailableSettings.SHOW_SQL, "true");
    }

    /**
     * 备份数据库
     */
    @Bean(name = "backupDataSource")
    @ConditionalOnProperty(value = "config.backup-on-delete.enable", havingValue = "true")
    public DataSource backupDataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dbConfig.getUrl());
        dataSource.setUsername(dbConfig.getUsername());
        dataSource.setPassword(dbConfig.getPassword());
        dataSource.setFilters("stat");
        dataSource.setMinIdle(2);
        dataSource.setValidationQuery("select 1");
        dataSource.setMaxActive(3);
        dataSource.setInitialSize(2);
        return dataSource;
    }

    @Bean(name = "backupEntityManagerFactory")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "config.backup-on-delete.enable", havingValue = "true")
    public LocalContainerEntityManagerFactoryBean backupEntityManagerFactory(ObjectProvider<PersistenceUnitManager> persistenceUnitManager,
                                                                             @Qualifier("backupDataSource") DataSource dataSource) {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(adapter,
                this.properties, persistenceUnitManager.getIfAvailable());

        List<Class<?>> classes = new ArrayList<>();
        classes.add(FlagForMiyaCommonModule.class);
        this.packages.ifPresent(classes::addAll);

        return builder
                .dataSource(dataSource)
                .packages(classes.toArray(new Class[0]))
                .properties(properties)
                .persistenceUnit("bodPersistenceUnit")
                .build();
    }
    /**
     * 配置事物管理器
     */
    @Bean(name = "backupTransactionManager")
    @ConditionalOnProperty(value = "config.backup-on-delete.enable", havingValue = "true")
    public PlatformTransactionManager backupTransactionManager(@Qualifier("backupEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "config.backup-on-delete")
    @Configuration
    public static class DbConfig {
        private String url;
        private String username;
        private String password;
        /**
         * 是否开启备份库
         */
        private boolean enable;
    }

}
