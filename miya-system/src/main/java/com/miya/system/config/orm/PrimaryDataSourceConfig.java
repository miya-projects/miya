package com.miya.system.config.orm;

import cn.hutool.core.util.StrUtil;
import com.miya.common.module.base.ExtendsRepositoryImpl;
import com.miya.system.module.FlagForMiyaSystemModule;
import com.miya.system.module.search.elastic.ElasticConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * 额外需要扫描的包，可通过该点进行entityManager扫描包扩展
     */
    @Resource
    @Qualifier("ormPackages")
    private Optional<List<Class<?>>> packages;

    @Resource
    private JpaProperties jr;

    @Resource
    private ElasticConfigProperties elasticConfigProperties;

    @Primary
    @Bean(name = "entityManagerFactory")
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(FlagForMiyaSystemModule.class);
        this.packages.ifPresent(classes::addAll);

        Map<String, String> properties = jr.getProperties();
        properties.put("hibernate.search.enabled", "false");
        if (elasticConfigProperties != null && StrUtil.isNotBlank(elasticConfigProperties.getHosts())){
            properties.put("hibernate.search.enabled", "true");
            properties.put("hibernate.search.backend.hosts", elasticConfigProperties.getHosts());
            properties.put("hibernate.search.backend.protocol", elasticConfigProperties.getProtocol());
            properties.put("hibernate.search.backend.username", elasticConfigProperties.getUsername());
            properties.put("hibernate.search.backend.password", elasticConfigProperties.getPassword());
        }
        return builder
                .dataSource(dataSource)
                .packages(classes.toArray(new Class[0]))
                .properties(jr.getProperties())
                .persistenceUnit("persistenceUnit")
                .build();
    }


}
