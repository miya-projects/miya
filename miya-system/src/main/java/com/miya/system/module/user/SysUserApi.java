package com.miya.system.module.user;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.base.BaseApi;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.model.SysUserDetailDTO;
import com.miya.system.module.user.model.SysUserListDTO;
import com.miya.system.module.user.model.SysUserForm;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * @author 杨超辉
 * @date 2018/6/26
 * @description
 */
@RequestMapping(value = "/user")
@RestController
@Slf4j
@Api(tags = {"用户"})
@Acl(userType = SysUser.class)
@Validated
public class SysUserApi extends BaseApi {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysUserRepository sysUserRepository;

    /**
     * 设置用户角色
     * @param roleIds 要设置的角色id
     */
    @PutMapping(value = "role")
    @ApiOperation(value = "设置用户角色", notes = "设置用户角色")
    public R<?> setRoles(@RequestParam(required = false) SysRole[] roleIds,
                         @ApiParam("用户id") @RequestParam("id") SysUser user) {
        sysUserService.setRoles(roleIds, user);
        return R.success();
    }

    /**
     * 用户列表
     * @return
     */
    @ApiOperation("用户列表")
    @GetMapping
    @Acl(business = "sys:user:view")
    public R<Grid<SysUserListDTO>> list(
            @QuerydslPredicate(root = SysUser.class) Predicate predicate,
            @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC) Pageable pageRequest) {
        // QSysUser qSysUser = QSysUser.sysUser;
        // SimpleEntityPathResolver simpleEntityPathResolver = SimpleEntityPathResolver.INSTANCE;
        // EntityPath<SysUser> path = simpleEntityPathResolver.createPath(SysUser.class);
        // Path<?> p = (Path<?>) ReflectUtil.getFieldValue(qSysUser, "username");
        // JPAQuery<SysUserDTO> query = queryFactory.select(
        //         Projections.bean(SysUserDTO.class, p, qSysUser.name)
        // ).from(qSysUser).where(predicate);
        // Page<SysUserDTO> page = PageableExecutionUtils.getPage(query.fetch(), pageRequest, () -> sysUserRepository.count(predicate));
        // SimpleExpression<SysUser> wqe = QSysUser.sysUser.as("wqe");
        Page<SysUser> all = sysUserRepository.findAll(predicate, pageRequest);
        return R.successWithData(Grid.of(all.map(SysUserListDTO::of)));
    }

    @PostMapping
    @Acl(business = "sys:user:add")
    @ApiOperation("新增用户")
    public R<?> save(@Validated SysUserForm sysUser) {
        return sysUserService.save(sysUser);
    }

    @PutMapping("{id}")
    @Acl(business = "sys:user:edit")
    @ApiOperation("用户修改")
    public R<?> update(@Validated SysUserForm sysUserForm, @PathVariable("id") @NotNull SysUser user) {
        sysUserService.update(sysUserForm, user);
        return R.success();
    }

    @GetMapping("{id}")
    @Acl(business = "sys:user:view")
    @ApiOperation("用户详情")
    public R<SysUserListDTO> detail(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return R.successWithData(SysUserDetailDTO.of(sysUser));
    }

    @ApiOperation("删除用户")
    @DeleteMapping("{id}")
    @Acl(business = "sys:user:delete")
    public R<?> delete(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        sysUserService.delete(sysUser);
        return R.success();
    }

    /**
     * 冻结用户
     * {@link ResponseCode}
     */
    @ApiOperation("冻结用户")
    @PutMapping("{id}/blocks")
    @Acl(business = "sys:user:blocks")
    public R<?> freeze(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        if (sysUser.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.CAN_NOT_OPERATE_SUPER_ADMIN);
        }
        sysUser.setAccountStatus(SysUser.AccountStatus.LOCKED);
        sysUserRepository.save(sysUser);
        return R.success();
    }

    /**
     * 解冻用户
     * @see ResponseCode
     */
    @ApiOperation("解冻用户")
    @DeleteMapping("{id}/blocks")
    @Acl(business = "sys:user:blocks")
    public R<?> unFreeze(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        sysUser.setAccountStatus(SysUser.AccountStatus.NORMAL);
        sysUserRepository.save(sysUser);
        return R.success();
    }

    /**
     * 重置别人的密码
     * @param sysUser
     * @return
     */
    @ApiOperation("重置密码")
    //    @PreAuthorize("hasAuthority('user')")
    @PutMapping("{id}/password")
    @Acl(business = "sys:user:resetPassword")
    public R<String> resetPassword(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return R.successWithData(sysUserService.resetPassword(sysUser));
    }

}
