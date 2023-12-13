package com.miya.common.module.init;

import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.config.SystemConfigKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitSystemService {

    private final List<SystemInit> systemInits;
    private final SysConfigService sysConfigService;
    private final JpaProperties jr;

    private static final List<String> ddl = Arrays.asList("update", "create", "create-drop", "create-only");

    /**
     * 初始化系统
     */
    @PostConstruct
    public void init() {
        String ddlAuto = jr.getProperties().get(AvailableSettings.HBM2DDL_AUTO);
        if (ddl.contains(ddlAuto)) {
            return;
        }
        Boolean isInitialize = SystemConfigKeys.IS_INITIALIZE.getValue();
        if (isInitialize) {
            return;
        }
        log.info("正在初始化系统...");
        for (SystemInit systemInit : systemInits) {
            systemInit.init();
        }
        sysConfigService.put(SystemConfigKeys.IS_INITIALIZE.name(), "true", SystemConfigKeys.IS_INITIALIZE.getName(), "SYSTEM");
        log.info("初始化完成");
    }

}
