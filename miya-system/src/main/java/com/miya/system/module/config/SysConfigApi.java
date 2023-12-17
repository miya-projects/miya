package com.miya.system.module.config;


import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.base.BaseApi;
import com.miya.common.module.config.*;
import com.miya.system.module.user.model.SysUserPrincipal;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统参数
 */
@RequestMapping("/config")
@RestController
@Slf4j
@Tag(name = "参数")
@Acl(userType = SysUserPrincipal.class)
@Validated
public class SysConfigApi extends BaseApi {

    @Resource
    private SysConfigRepository sysConfigRepository;
    @Resource
    private SysConfigService sysConfigService;

    @Operation(summary = "系统参数")
    @GetMapping("internal")
    public R<List<SysConfigDTO>> systemConfigs(){
        List<SysConfig> configs = sysConfigService.getConfigsByGroup(SysConfig.GROUP_SYSTEM);
        List<SysConfigDTO> list = configs.stream().map(SysConfigDTO::of).collect(Collectors.toList());
        return R.successWithData(list);
    }

    /**
     * 设置系统参数
     */
    @PutMapping
    @Operation(summary = "设置系统参数")
    public R<SysConfig> setup(
            @NotNull String group,
            @NotNull String configKey,
            String value,
            @AuthenticationPrincipal SysUserPrincipal user) {
        if (!user.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.NOT_ADMIN);
        }
        sysConfigService.set(group, configKey, value);
        return R.success();
    }

    /**
     * 系统参数列表
     */
    @Operation(summary = "系统参数列表")
    @GetMapping
    public R<?> list(
            @QuerydslPredicate(root = SysConfig.class) Predicate predicate,
            Pageable pageRequest) {
        Page<SysConfig> all = sysConfigRepository.findAll(predicate, pageRequest);
        return R.successWithData(Grid.of(all));
    }

    /**
     * 系统参数详情
     */
    @Operation(summary = "系统参数详情")
    @GetMapping("{id}")
    public R<SysConfig> detail(@NotNull(message = "id不合法") @PathVariable(value = "id") SysConfig sysConfig) {
        return R.successWithData(sysConfig);
    }

    /**
     * 修改系统参数
     */
    @Operation(summary = "修改系统参数")
    @PutMapping("{id}")
    public R<SysConfig> update(@NotNull(message = "id不合法") @PathVariable(value = "id") SysConfig sysConfig,
                    @Validated SysConfigForm sysConfigForm,
                    @AuthenticationPrincipal SysUserPrincipal user) {
        if (!user.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.NOT_ADMIN);
        }
        QSysConfig qSysConfig = QSysConfig.sysConfig;
        long count = sysConfigRepository.count(qSysConfig.group.eq(sysConfigForm.getGroup()).and(qSysConfig.key.eq(sysConfigForm.getKey()))
                .and(qSysConfig.id.ne(sysConfig.getId())));
        if (count > 0) {
            return R.errorWithMsg("分组和key重复");
        }
        sysConfigForm.mergeToPo(sysConfig);
        sysConfigRepository.save(sysConfig);
        return R.successWithData(sysConfig);
    }

    /**
     * 新增系统参数
     */
    @PostMapping
    @Operation(summary = "新增系统参数")
    public R<SysConfig> save(
            @Validated SysConfigForm sysConfigForm,
            @AuthenticationPrincipal SysUserPrincipal user) {
        if (!user.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.NOT_ADMIN);
        }
        boolean exists = sysConfigRepository.exists(QSysConfig.sysConfig.key.eq(sysConfigForm.getKey()));
        if (exists) {
            return R.errorWithMsg("key重复");
        }
        SysConfig sysConfig = sysConfigForm.mergeToNewPo();
        sysConfigRepository.save(sysConfig);
        return R.successWithData(sysConfig);
    }

    /**
     * 删除系统参数
     */
    @DeleteMapping("{id}")
    @Operation(summary = "删除系统参数")
    public R<?> delete(
            @PathVariable String id,
            @AuthenticationPrincipal SysUserPrincipal user) {
        if (!user.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.NOT_ADMIN);
        }
        sysConfigRepository.deleteById(id);
        return R.success();
    }

    /**
     * 重载所有参数
     */
    @GetMapping("flush")
    @Operation(summary = "重载所有参数")
    public R<?> flush(@AuthenticationPrincipal SysUserPrincipal user) {
        if (!user.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.NOT_ADMIN);
        }
        // todo
        sysConfigService.cleanAllCache();
        return R.success();
    }

}
