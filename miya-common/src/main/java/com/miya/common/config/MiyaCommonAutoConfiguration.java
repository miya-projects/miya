package com.miya.common.config;

import com.miya.common.config.orm.source.CommonDataSourceConfig;
import com.miya.common.module.bod.BackupDataSourceConfig;
import com.miya.common.module.cache.CacheConfig;
import com.miya.common.module.config.InitSystem;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.sms.SmsConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ComponentScan(
        basePackageClasses = {
                ScanFlag.class
        }
)
@Import({
        SmsConfig.class,
        CacheConfig.class,
        CommonDataSourceConfig.class,
        BackupDataSourceConfig.class,
        SysConfigService.class,
        InitSystem.class
})
@Configuration
public class MiyaCommonAutoConfiguration {

}
