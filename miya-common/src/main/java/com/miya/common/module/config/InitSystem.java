package com.miya.common.module.config;

import com.miya.common.module.init.SystemInit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitSystem {

    private final List<SystemInit> systemInits;
    private final SysConfigService sysConfigService;
    private final JpaProperties jr;

    private static final List<String> ddl = Arrays.asList("update", "create", "create-drop", "create-only");

    /**
     * 初始化系统
     */
    @PostConstruct
    public void init(){
        String ddlauto = jr.getProperties().get(AvailableSettings.HBM2DDL_AUTO);
        if (ddl.contains(ddlauto)){
            Optional<String> isInitialize = sysConfigService.get(SysConfigService.SystemConfigKey.IS_INITIALIZE);
            if (!isInitialize.isPresent()) {
                log.info("正在初始化系统...");
                // 系统还未初始化，需要初始化
                for (SystemInit systemInit : systemInits) {
                    systemInit.init();
                }
                log.info("初始化完成");
            }

        }

    }

}
