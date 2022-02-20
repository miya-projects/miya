package com.miya.common.config.orm.source;

import com.alibaba.druid.pool.DruidDataSource;
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
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
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
import javax.sql.DataSource;
import java.sql.SQLException;
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
public class CommonDataSourceConfig implements InitializingBean {

    @Resource
    private DbConfig dbConfig;
    /**
     * 额外需要扫描的包，可通过该点进行entityManager扫描包扩展
     */
    @Resource
    @Qualifier("ormPackages")
    private Optional<List<Class<?>>> packages;

    @Resource
    private JpaProperties jr;

    @Override
    public void afterPropertiesSet() {
        Map<String, String> properties = jr.getProperties();
        properties.put(AvailableSettings.DIALECT, ExtensionMySQLDialect.class.getName());
        properties.put(AvailableSettings.FLUSH_MODE, FlushMode.COMMIT.name());
        // properties.put(AvailableSettings.INTERCEPTOR, AuditingLogListener.class.getName());
        properties.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, "20");
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy.class.getName());
        // properties.put(AvailableSettings.HBM2DDL_AUTO, "update");

        // 对有加注解的实体启用二级缓存
        properties.put(AvailableSettings.JPA_SHARED_CACHE_MODE, SharedCacheMode.ENABLE_SELECTIVE.name());
        properties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, Boolean.TRUE.toString());
        properties.put(AvailableSettings.USE_QUERY_CACHE, Boolean.TRUE.toString());
        // QUERY_CACHE_FACTORY
        properties.put(AvailableSettings.CACHE_REGION_PREFIX, "cache_l2_");
        // 结构化二级缓存数据，用于DEBUG，会对性能产生一定影响
        properties.put(AvailableSettings.USE_STRUCTURED_CACHE, Boolean.FALSE.toString());
        // 二级缓存实现
        properties.put(AvailableSettings.CACHE_REGION_FACTORY, EhcacheRegionFactory.class.getName());
        //        properties.put(AvailableSettings.CONNECTION_HANDLING, PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION.name());
        //         properties.put("hibernate.integrator_provider",
        //                 (IntegratorProvider) () -> Collections.singletonList( MetadataExtractorIntegrator.INSTANCE));

    }

    @Primary
    @Bean(name = "dataSource")
    @ConditionalOnMissingBean(name = "dataSource")
    public DataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dbConfig.getUrl());
        dataSource.setUsername(dbConfig.getUsername());
        dataSource.setPassword(dbConfig.getPassword());
        dataSource.setFilters("stat");
        dataSource.setMinIdle(2);
        dataSource.setValidationQuery("select 1");
        dataSource.setMaxActive(3);
        dataSource.setInitialSize(2);
//        dataSource.setWe
        // dataSource.setRemoveAbandoned(true);
        // dataSource.setLogAbandoned(true);
        // dataSource.setRemoveAbandonedTimeout(180);
        // dataSource.setMaxWait(30000);
        return dataSource;
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(FlagForMiyaCommonModule.class);
        this.packages.ifPresent(classes::addAll);
        return builder
                .dataSource(dataSource)
                .packages(classes.toArray(new Class[0]))
                .properties(jr.getProperties())
                .persistenceUnit("persistenceUnit")
                .build();
    }

    /**
     * 配置事物管理器
     * @param entityManagerFactory
     */
    @Bean(name = "transactionManager")
    @Primary
    @ConditionalOnMissingBean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "config.db")
    @Configuration
    public static class DbConfig {
        private String url;
        private String username;
        private String password;
    }

}
