package com.miya.system.service;

import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.config.SystemConfigKeys;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SysConfigServiceTest {
    @Resource
    private SysConfigService sysConfigService;

    @Test
    public void getBackendDomain() {
        String backendDomain = sysConfigService.get(SystemConfigKeys.BACKEND_DOMAIN);
        log.info(backendDomain);
    }

    @Test
    public void get() {

    }

    @Test
    public void set() {
    }

    @Test
    public void getDefaultEmailAccount() {
    }

    @Test
    public void getEmailAccounts() {
    }

    @Test
    public void getEmailAccount() {
    }

    @Test
    public void testGetEmailAccounts() {
    }

    @Test
    public void testGetEmailAccount() {
    }
}
