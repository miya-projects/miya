package com.miya.common.config;

import com.miya.common.annotation.constraint.CustomMessageInterpolator;
import com.miya.common.config.orm.source.CommonDataSourceConfig;
import com.miya.common.module.cache.CacheConfig;
import com.miya.common.module.init.InitSystemService;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.util.TransactionUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import jakarta.validation.Validator;

@ComponentScan(
        basePackageClasses = {
                ScanFlag.class
        }
)
@Import({
        InitSystemService.class,
        CacheConfig.class,
        CommonDataSourceConfig.class,
        SysConfigService.class,
        TransactionUtil.class,
})
@AutoConfiguration
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
