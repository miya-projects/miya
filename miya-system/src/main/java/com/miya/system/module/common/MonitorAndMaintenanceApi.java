package com.miya.system.module.common;

import cn.hutool.core.map.MapUtil;
import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

import static cn.hutool.core.map.MapUtil.entry;
/**
 * 用于为服务监控程序提供健康检查，如traefik,k8s等
 */
@Slf4j
@Acl(userType = Acl.NotNeedLogin.class)
@RequiredArgsConstructor
@Validated
@Api(tags = {"监控"})
@RestController("/monitorAndMaintenance")
public class MonitorAndMaintenanceApi {

    @Value("#{sysConfig['SYSTEM_NAME']}")
    private Supplier<String> systemName;

    @Value("#{sysConfig['SYSTEM_VERSION']}")
    private Supplier<String> version;


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
    @CrossOrigin(originPatterns = "*", allowCredentials = "true", methods = {RequestMethod.GET})
    public R<?> version() {
        return R.successWithData(MapUtil.<String, String>ofEntries(
                entry("name", systemName.get()),
                entry("version", version.get())
        ));
    }

}
