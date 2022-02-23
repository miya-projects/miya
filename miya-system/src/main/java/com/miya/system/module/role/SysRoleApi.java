package com.miya.system.module.role;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseApi;
import com.miya.system.config.business.Business;
import com.miya.system.config.business.SystemErrorCode;
import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.role.model.SysRoleDTO;
import com.miya.system.module.role.model.SysRoleForm;
import com.miya.system.module.user.model.SysUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色api
 * @author 杨超辉
 **/
@RequestMapping(value = "role")
@Slf4j
@RestController
@Api(tags = {"角色"})
@Acl(userType = SysUser.class)
@Validated
public class SysRoleApi extends BaseApi {

    @Resource
    private SysRoleRepository sysRoleRepository;
    @Resource
    private SysRoleService sysRoleService;


    /**
     * 角色列表
     */
    @ApiOperation(value = "角色列表")
    @GetMapping
    @Acl(business = "sys:role:view")
    public R<?> list(@PageableDefault(sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SysRole> all = sysRoleRepository.findAll(pageable);
        return R.successWithData(Grid.of(all.map(SysRoleDTO::of)));
    }

    /**
     * 角色详情
     */
    @ApiOperation(value = "角色详情")
    @GetMapping(value = "{id}")
    @Acl(business = "sys:role:view")
    public R<?> detail( @ApiParam("角色id") @NotNull @PathVariable("id") SysRole role) {
        return R.successWithData(SysRoleDTO.of(role));
    }

    /**
     * 为角色设置权限
     * @param codes     权限code集合
     * @param sysRole 角色id
     */
    @ApiOperation(value = "为角色设置权限")
    @PutMapping(value = "{id}/business")
    @Acl(business = "sys:role:edit")
    public R<?> setPermissions(@ApiParam("权限code") String[] codes,
                            @ApiParam("角色id") @NotNull @PathVariable("id") SysRole sysRole,
                            @AuthenticationPrincipal SysUser sysUser) {
        if (sysRole.getIsSystem() && !sysUser.isSuperAdmin()){
            // 只有超级管理员才可以配置系统角色的权限
            return R.errorWithCodeAndMsg(SystemErrorCode.OPE_SYSTEM_ROLE);
        }
        sysRoleService.saveBusiness(sysRole, codes);
        return R.success();
    }


    /**
     * 角色增加
     * @param sysRoleForm 角色信息
     */
    @PostMapping
    @ApiOperation("增加角色")
    @Acl(business = "sys:role:add")
    public R<?> save( @Validated SysRoleForm sysRoleForm) {
        return sysRoleService.saveRole(sysRoleForm);
    }

    /**
     * 更新
     * @param sysRoleForm 角色信息
     */
    @PutMapping(value = "{id}")
    @ApiOperation("修改角色")
    @Acl(business = "sys:role:edit")
    public R<?> update(@Validated SysRoleForm sysRoleForm, @ApiParam("角色id") @PathVariable("id") @NotNull SysRole sysRole) {
        return sysRoleService.updateRole(sysRoleForm, sysRole);
    }

    /**
     * 角色删除
     * @param sysRole 角色id
     */
    @ApiOperation("删除角色")
    @DeleteMapping(value = "{id}")
    @Acl(business = "sys:role:delete")
    public R<?> delete( @ApiParam("角色id") @PathVariable("id") @NotNull SysRole sysRole ) {
        return sysRoleService.deleteRole(sysRole);
    }

    /**
     * 获取业务功能列表
     */
    @GetMapping(value = "/business")
    @ApiOperation("获取业务功能列表")
    public R<?> businessList() {
        List<Business> businesses = sysRoleService.getBusiness();
        List<Business> collect = businesses.stream().filter(business -> Objects.isNull(business.getParent()))
                .collect(Collectors.toList());
        return R.successWithData(collect);
    }
}
