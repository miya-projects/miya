package com.miya.system.module.common;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.config.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于为服务监控程序提供健康检查，如traefik,k8s等
 */
@Slf4j
@Acl(userType = Acl.NotNeedLogin.class)
@AllArgsConstructor
@Validated
@Api(tags = {"监控"})
@RestController("/monitorAndMaintenance")
public class MonitorAndMaintenanceApi {

    private final SysConfigService configService;

    /**
     * 健康检查
     * 挂在根路径 /api/health
     */
    @ApiOperation("健康检查")
    @GetMapping(value = "/health")
    public R<?> health() {
        return R.success();
    }

    /**
     * 软件版本信息
     * 挂在根路径 /api/version
     */
    @ApiOperation("软件版本信息")
    @GetMapping(value = "/version")
    public R<?> version() {
        return R.successWithData(MapUtil.<String, String>of(
                Pair.of("name", configService.getSystemName()),
                Pair.of("version", configService.get("SYSTEM_VERSION").get())
        ));
    }

}
