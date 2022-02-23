package com.miya.system.module.user;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.base.BaseApi;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.model.SysUserDetailDTO;
import com.miya.system.module.user.model.SysUserListDTO;
import com.miya.system.module.user.model.SysUserForm;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * @author 杨超辉
 */
@RequestMapping(value = "/user")
@RestController
@Slf4j
@Api(tags = {"用户"})
@Acl(userType = SysUser.class)
@Validated
@RequiredArgsConstructor
public class SysUserApi extends BaseApi {

    private final SysUserService sysUserService;
    private final SysUserRepository sysUserRepository;

    /**
     * 用户列表
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

    /**
     * 用户列表导出
     */
    @ApiOperation("用户列表导出")
    @GetMapping("export")
    @Acl(business = "sys:user:download")
    public void export(
            @QuerydslPredicate(root = SysUser.class) Predicate predicate, HttpServletResponse response) throws IOException {
        sysUserService.export(predicate, response);
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
        return sysUserService.freeze(sysUser);
    }

    /**
     * 解冻用户
     * @see ResponseCode
     */
    @ApiOperation("解冻用户")
    @DeleteMapping("{id}/blocks")
    @Acl(business = "sys:user:blocks")
    public R<?> unFreeze(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return sysUserService.unFreeze(sysUser);
    }

    /**
     * 重置别人的密码
     * @param sysUser
     */
    @ApiOperation("重置密码")
    //    @PreAuthorize("hasAuthority('user')")
    @PutMapping("{id}/password")
    @Acl(business = "sys:user:resetPassword")
    public R<String> resetPassword(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return R.successWithData(sysUserService.resetPassword(sysUser));
    }

}
