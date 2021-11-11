package com.miya.system.module.user;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import com.miya.common.annotation.Acl;
import com.miya.common.annotation.RequestLimit;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.sms.service.SmsService;
import com.miya.common.module.config.SysConfigService;
import com.miya.system.module.notice.SysNoticeService;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.model.SysUserDetailDTO;
import com.miya.system.module.user.model.SysUserModifyForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * @author 杨超辉
 * @date 2018/6/26
 */
@RestController
@RequestMapping("user/current")
@Slf4j
@Api(tags = {"账户"})
@Acl(userType = SysUser.class)
@Validated
public class AccountApi {

    @Resource
    private SysUserService sysUserService;
    @Resource
    private SmsService smsService;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private SysNoticeService sysNoticeService;

    /**
     * 登陆
     * @param userName 用户名
     * @param password 密码
     */
    @PostMapping(value = "login")
    @ApiOperation(value = "后台登录", notes = "后台登录接口")
    @Acl(userType = Acl.NotNeedLogin.class)
    public R<SysUserService.LoginRes> login(@ApiParam(value = "用户名", example = "admin") @NotBlank String userName,
                                            @ApiParam(value = "密码", example = "123456") @NotBlank String password) {
        return R.successWithData(sysUserService.login(userName, password));
    }

    /**
     * 登陆
     * @param phone 手机号
     * @param verifyCode 密码
     */
    @PostMapping(value = "loginByPhone")
    @ApiOperation(value = "通过手机号验证码登录", notes = "后台登录接口")
    @Acl(userType = Acl.NotNeedLogin.class)
    public R<SysUserService.LoginRes> loginByPhone(@ApiParam(value = "手机号", example = "13800000000") @NotBlank String phone,
                                                   @ApiParam(value = "验证码", example = "123456") @NotBlank String verifyCode) {
        return R.successWithData(sysUserService.loginByPhone(phone, verifyCode));
    }

    /**
     * 登陆
     * @param phone 手机号
     */
    @PostMapping(value = "sendVerifyCode")
    @ApiOperation(value = "发送手机验证码")
    @Acl(userType = Acl.NotNeedLogin.class)
    @RequestLimit(count = 1)
    public R<SysUserService.LoginRes> sendVerifyCode(@ApiParam(value = "手机号", example = "13800000000") @NotBlank String phone) {
        String verifyCode = RandomUtil.randomNumbers(6);
        smsService.sendVerifyCode(phone, verifyCode);
        return R.success();
    }

    @ApiOperation(value = "获取用户信息和偏好配置", notes = "每次重新打开首页便获取一次")
    @GetMapping
    public R<?> current(@AuthenticationPrincipal final SysUser sysUser) {
        //        QSysUser qSysUser = QSysUser.sysUser;
        //        Projections.fields();
        //        Criteria.LEFT_JOIN;
        //        JoinType.LEFT_OUTER_JOIN
        //        entityManager.getCriteriaBuilder().toBigInteger()
        //        SysUserForm sysUserForm = qf.select(
        //                Projections.bean(SysUserForm.class, qSysUser)
        //        ).from(qSysUser).where(qSysUser.id.eq(sysUser.getId())).fetchOne();

        return R.successWithData(MapUtil.<String, Object>of(
                Pair.of("user", SysUserDetailDTO.of(sysUser)),
                Pair.of("systemMeta", sysConfigService.getSystemMeta()),
                Pair.of("unreadNoticeAmount", sysNoticeService.unreadNoticeAmount(sysUser.getId()))
        ));
    }

    @PutMapping("preferences")
    @ApiOperation(value = "用户偏好配置修改(前端)")
    public R<?> preferences(@NotBlank String preferences, @AuthenticationPrincipal final SysUser sysUser) {
        sysUser.getPreferences().setFront(preferences);
        sysUserService.update(sysUser);
        return R.success();
    }

    @Acl
    @PutMapping(value = "password")
    @ApiOperation("当前用户使用旧密码修改密码")
    public R<?> modifyPassword(@ApiParam("旧密码") @NotBlank(message = "旧密码不能为空") String password,
                               @ApiParam("新密码") @NotBlank(message = "新密码不能为空") String newPassword,
                               @AuthenticationPrincipal SysUser user) {
        sysUserService.modifyPassword(user, password, newPassword);
        return R.success();
    }

    @Acl
    @PutMapping
    @ApiOperation(value = "个人信息修改", notes = "修改当前用户的信息")
    public R<?> update(@Validated SysUserModifyForm userModifyForm,
                               @AuthenticationPrincipal SysUser user) {
        sysUserService.modifyProfile(userModifyForm, user);
        return R.success();
    }



}
