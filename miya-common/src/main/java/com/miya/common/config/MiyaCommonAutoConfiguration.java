package com.miya.common.config;

import com.miya.common.annotation.constraint.CustomMessageInterpolator;
import com.miya.common.config.orm.source.CommonDataSourceConfig;
import com.miya.common.config.orm.source.DataSourceConfig;
import com.miya.common.module.bod.BackupDataSourceConfig;
import com.miya.common.module.cache.CacheConfig;
import com.miya.common.module.config.InitSystem;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.sms.SmsConfig;
import com.miya.common.util.TransactionUtil;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import javax.validation.Validator;

@ComponentScan(
        basePackageClasses = {
                ScanFlag.class
        }
)
@Import({
        InitSystem.class,
        SmsConfig.class,
        CacheConfig.class,
        CommonDataSourceConfig.class,
        BackupDataSourceConfig.class,
        SysConfigService.class,
        TransactionUtil.class,
        DataSourceConfig.class
})
@Configuration
@AutoConfigureBefore(ValidationAutoConfiguration.class)
public class MiyaCommonAutoConfiguration {

        /**
         * 自定义插值器
         */
        @Bean
        public Validator validator() {
                LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
                localValidatorFactoryBean.setMessageInterpolator(new CustomMessageInterpolator());
                return localValidatorFactoryBean;
        }

}
