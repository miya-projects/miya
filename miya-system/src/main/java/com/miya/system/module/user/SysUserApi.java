package com.miya.system.module.user;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.base.BaseApi;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.user.model.*;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author 杨超辉
 */
@RequestMapping(value = "/user")
@RestController
@Slf4j
@Tag(name = "用户")
@Acl(userType = SysUserPrincipal.class)
@Validated
@RequiredArgsConstructor
public class SysUserApi extends BaseApi {

    private final SysUserService sysUserService;
    private final SysUserRepository sysUserRepository;

    /**
     * 用户列表
     */
    @Operation(summary = "用户列表11")
    @GetMapping("123")
    public R<SysUserListDTO> lis1t() {
        return null;
    }

    /**
     * 用户列表
     */
    @Operation(summary = "用户列表")
    @GetMapping
    @Acl(business = "sys:user:view")
    public R<Grid<SysUserListDTO>> list(
            @QuerydslPredicate(root = SysUser.class) Predicate predicate,
            @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC) Pageable pageable) {
        // QSysUser qSysUser = QSysUser.sysUser;
        // SimpleEntityPathResolver simpleEntityPathResolver = SimpleEntityPathResolver.INSTANCE;
        // EntityPath<SysUser> path = simpleEntityPathResolver.createPath(SysUser.class);
        // Path<?> p = (Path<?>) ReflectUtil.getFieldValue(qSysUser, "username");
        // JPAQuery<SysUserDTO> query = queryFactory.select(
        //         Projections.bean(SysUserDTO.class, p, qSysUser.name)
        // ).from(qSysUser).where(predicate);
        // Page<SysUserDTO> page = PageableExecutionUtils.getPage(query.fetch(), pageable, () -> sysUserRepository.count(predicate));
        // SimpleExpression<SysUser> wqe = QSysUser.sysUser.as("wqe");

        // qf.select(
        //         Projections.bean(SysUserListDTO.class, QSysUser.sysUser.name)
        // ).from(QSysUser.sysUser).fetch()
        // Projections.constructor()

        // QBean qBean = new QBean(SysUser.class);
        // new DefaultRepositoryInvokerFactory(new Repositories(SpringUtil.getApplicationContext()), SpringUtil.getBean(ConversionService.class)).getInvokerFor(SysUser.class).invokeFindById("1");
        Page<SysUser> all = sysUserRepository.findAll(predicate, pageable);
        return R.successWithData(Grid.of(all.map(SysUserListDTO::of)));
    }

    /**
     * 用户列表导出
     */
    @Operation(summary = "用户列表导出")
    @GetMapping("export")
    @Acl(business = "sys:user:download")
    public void export(
            @AuthenticationPrincipal SysUserPrincipal user,
            @QuerydslPredicate(root = SysUser.class) Predicate predicate, HttpServletResponse response) throws IOException {
        sysUserService.export(predicate, response, user.toPO());
    }

    @PostMapping
    @Acl(business = "sys:user:add")
    @Operation(summary = "新增用户")
    public R<?> save(@Validated SysUserForm sysUser) {
        return sysUserService.save(sysUser);
    }

    @PutMapping("{id}")
    @Acl(business = "sys:user:edit")
    @Operation(summary = "用户修改")
    public R<?> update(@Validated SysUserForm sysUserForm, @PathVariable("id") @NotNull SysUser user) {
        sysUserService.update(sysUserForm, user);
        return R.success();
    }

    @GetMapping("{id}")
    @Acl(business = "sys:user:view")
    @Operation(summary = "用户详情")
    public R<SysUserListDTO> detail(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return R.successWithData(SysUserDetailDTO.of(sysUser));
    }

    @Operation(summary = "删除用户")
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
    @Operation(summary = "冻结用户")
    @PutMapping("{id}/blocks")
    @Acl(business = "sys:user:blocks")
    public R<?> freeze(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return sysUserService.freeze(sysUser);
    }

    /**
     * 解冻用户
     * @see ResponseCode
     */
    @Operation(summary = "解冻用户")
    @DeleteMapping("{id}/blocks")
    @Acl(business = "sys:user:blocks")
    public R<?> unFreeze(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return sysUserService.unFreeze(sysUser);
    }

    /**
     * 重置别人的密码
     * @param sysUser
     */
    @Operation(summary = "重置密码")
    @PutMapping("{id}/password")
    @Acl(business = "sys:user:resetPassword")
    public R<String> resetPassword(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return R.successWithData(sysUserService.resetPassword(sysUser));
    }

    /**
     * 以他人的身份登录
     */
    @Operation(summary = "以他人的身份登录")
    @PutMapping("{id}/loginAs")
    @Acl(business = "sys:user:loginAs")
    public R<SysUserService.LoginRes> loginAs(@NotNull(message = "id不合法") @PathVariable(value = "id") SysUser sysUser) {
        return R.successWithData(sysUserService.loginAs(sysUser));
    }

}
