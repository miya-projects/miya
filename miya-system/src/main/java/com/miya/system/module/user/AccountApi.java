package com.miya.system.module.user;

import cn.hutool.core.util.RandomUtil;
import com.miya.common.annotation.Acl;
import com.miya.common.annotation.RequestLimit;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.sms.service.SmsService;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.dto.SysUserModifyForm;
import com.miya.system.module.user.model.SysUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/**
 * @author 杨超辉
 */
@RestController
@RequestMapping("user/current")
@Slf4j
@Tag(name = "账户")
@Acl(userType = SysUserPrincipal.class)
@Validated
public class AccountApi {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private SmsService smsService;

    /**
     * 登陆
     * @param userName 用户名
     * @param password 密码
     */
    @PostMapping(value = "login")
    @Operation(summary = "后台登录", description = "后台登录接口")
    @Acl(userType = Acl.NotNeedLogin.class)
    public R<SysUserService.LoginRes> login(@Parameter(description = "用户名", example = "admin") @NotBlank String userName,
                                            @Parameter(description = "密码", example = "123456") @NotBlank String password) {
        return R.successWithData(sysUserService.login(userName, password));
    }

    /**
     * 登陆
     * @param phone 手机号
     * @param verifyCode 密码
     */
    @PostMapping(value = "loginByPhone")
    @Operation(summary = "通过手机号验证码登录", description = "后台登录接口")
    @Acl(userType = Acl.NotNeedLogin.class)
    public R<SysUserService.LoginRes> loginByPhone(@Parameter(description = "手机号", example = "13800000000") @NotBlank String phone,
                                                   @Parameter(description = "验证码", example = "123456") @NotBlank String verifyCode) {
        return R.successWithData(sysUserService.loginByPhone(phone, verifyCode));
    }

    /**
     * 登陆
     * @param phone 手机号
     */
    @PostMapping(value = "sendVerifyCode")
    @Operation(summary = "发送手机验证码")
    @Acl(userType = Acl.NotNeedLogin.class)
    @RequestLimit(count = 1)
    public R<?> sendVerifyCode(@Parameter(description = "手机号", example = "13800000000") @NotBlank String phone) {
        String verifyCode = RandomUtil.randomNumbers(6);
        smsService.sendVerifyCode(phone, verifyCode);
        return R.success();
    }

    @Operation(summary = "获取用户信息和偏好配置", description = "每次重新打开首页便获取一次")
    @GetMapping
    public R<?> current(@AuthenticationPrincipal final SysUserPrincipal sysUserPrincipal) {
        //        QSysUser qSysUser = QSysUser.sysUser;
        //        Projections.fields();
        //        Criteria.LEFT_JOIN;
        //        JoinType.LEFT_OUTER_JOIN
        //        entityManager.getCriteriaBuilder().toBigInteger()
        //        SysUserForm sysUserForm = qf.select(
        //                Projections.bean(SysUserForm.class, qSysUser)
        //        ).from(qSysUser).where(qSysUser.id.eq(sysUser.getId())).fetchOne();
        return R.successWithData(sysUserService.current(sysUserPrincipal));
    }

    @PutMapping("preferences")
    @Operation(summary = "用户偏好配置修改(前端)")
    public R<?> preferences(@NotBlank String preferences, @AuthenticationPrincipal final SysUserPrincipal sysUserPrincipal) {
        SysUser sysUser = sysUserPrincipal.toPO();
        sysUser.getPreferences().setFront(preferences);
        sysUserService.update(sysUser);
        return R.success();
    }

    @PutMapping(value = "password")
    @Operation(summary = "当前用户使用旧密码修改密码")
    public R<?> modifyPassword(@Parameter(description = "旧密码") @NotBlank(message = "旧密码不能为空") String password,
                               @Parameter(description = "新密码") @NotBlank(message = "新密码不能为空") String newPassword,
                               @AuthenticationPrincipal SysUserPrincipal user) {
        sysUserService.modifyPassword(user.toPO(), password, newPassword);
        return R.success();
    }

    @PutMapping
    @Operation(summary = "个人信息修改", description = "修改当前用户的信息")
    public R<?> update(@Validated SysUserModifyForm userModifyForm,
                               @AuthenticationPrincipal SysUserPrincipal user) {
        sysUserService.modifyProfile(userModifyForm, user.toPO());
        return R.success();
    }



}
