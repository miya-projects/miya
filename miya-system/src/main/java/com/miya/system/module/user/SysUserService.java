package com.miya.system.module.user;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.miya.common.auth.way.GeneralAuthentication;
import com.miya.common.auth.way.LoginDevice;
import com.miya.common.auth.way.LoginWay;
import com.miya.common.config.web.jwt.JwtPayload;
import com.miya.common.config.web.jwt.TokenService;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.business.Business;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.sms.CacheKeys;
import com.miya.system.module.user.event.UserLoginEvent;
import com.miya.system.module.user.model.QSysUser;
import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.model.SysUserForm;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.common.service.JwtTokenService;
import com.miya.system.module.user.model.SysUserModifyForm;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserRepository sysUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;
    private final KeyValueStore keyValueStore;
    private final ProjectConfiguration projectConfiguration;
    private final JwtTokenService jwtTokenService;
    private final ApplicationContext ac;
    private final SysUserCustomizer customizer;

    private static final QSysUser qSysUser = QSysUser.sysUser;

    /**
     * 默认密码生成器
     */
    Function<SysUserForm, String> defaultPasswordGenerator = u -> "123456";

    /**
     * 重置密码时的密码生成器
     */
    Function<SysUser, String> passwordGeneratorForReset = u -> "123456";


    @PostConstruct
    public void customize(){
        this.defaultPasswordGenerator = customizer.passwordGeneratorForNewUser();
        this.passwordGeneratorForReset = customizer.passwordGeneratorForReset();
    }

    /**
     * 为用户设置角色
     * @param roleIds
     * @param user
     */
    public void setRoles(SysRole[] roleIds, SysUser user) {
        if (Objects.isNull(roleIds)) {
            roleIds = new SysRole[0];
        }
        HashSet<SysRole> sysRoles = new HashSet<>(Arrays.asList(roleIds));
        user.setRoles(sysRoles);
        sysUserRepository.save(user);
    }

    /**
     * 新增用户
     * @param userForm
     * @return
     */
    public R<?> save(SysUserForm userForm) {
        boolean exists = sysUserRepository.exists(qSysUser.username.eq(userForm.getUsername()));
        if (exists) {
            return R.errorWithMsg("用户名已被注册");
        }
        boolean existsSamePhone = sysUserRepository.exists(qSysUser.phone.eq(userForm.getPhone()));
        if (existsSamePhone){
            return R.errorWithMsg("手机号重复");
        }
        SysUser sysUser = userForm.mergeToNewPo();
        sysUser.setPassword(bCryptPasswordEncoder.encode(this.defaultPasswordGenerator.apply(userForm)));
        sysUserRepository.save(sysUser);
        return R.success();
    }

    /**
     * 修改当前用户个人信息
     * @param userModifyForm
     * @param user
     */
    public void modifyProfile(SysUserModifyForm userModifyForm, SysUser user) {
        userModifyForm.mergeToPo(user);
        sysUserRepository.save(user);
    }

    /**
     * 修改用户信息
     * @param sysUserForm
     * @param user
     */
    public void update(SysUserForm sysUserForm, SysUser user) {
        sysUserForm.mergeToPo(user);
        boolean existsSamePhone = sysUserRepository.exists(qSysUser.phone.eq(user.getPhone()).and(qSysUser.id.ne(user.getId())));
        if (existsSamePhone){
            throw new ErrorMsgException("手机号重复");
        }
        sysUserRepository.save(user);
    }

    /**
     * 删除用户
     * @param sysUser
     */
    public void delete(SysUser sysUser) {
        // todo 删除前的判断 删除用户封装
        if (sysUser.isSuperAdmin()) {
            throw new ResponseCodeException(ResponseCode.Common.CAN_NOT_OPERATE_SUPER_ADMIN);
        }
        sysUserRepository.delete(sysUser);
    }


    /**
     * 登录接口返回的dto
     */
    @Setter
    @Getter
    @Builder
    public static class LoginRes {
        private String token;
        /**
         * token失效日期
         */
        private Date expiredDate;
//        private SysUserDTO sysUserDTO;
    }

    /**
     * 通过手机号+验证码登录
     * @param phone  手机号
     * @param verifyCode  验证码
     */
    public LoginRes loginByPhone(String phone, String verifyCode) {
        Optional<SysUser> sysUserOptional = sysUserRepository.findOne(qSysUser.phone.eq(phone));
        if (!sysUserOptional.isPresent()) {
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "用户不存在");
        }
        SysUser sysUser = sysUserOptional.get();
        String rightVerifyCode = keyValueStore.get(CacheKeys.PHONE_VERIFY.toCacheKey(phone));
        if ((verifyCode != null) && (!StrUtil.equals(rightVerifyCode, verifyCode))){
            log.debug("登陆失败：用户{}验证码错误!", phone);
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "验证码错误");
        }
        if (sysUser.getAccountStatus().equals(SysUser.AccountStatus.LOCKED)) {
            throw new ErrorMsgException("账户被锁定");
        }
        // token有效期 一天
        DateTime expiredDate = DateUtil.offsetDay(new Date(), 1);
        String token = generateToken(sysUser, expiredDate, LoginWay.PHONE_AND_CODE);
        loginLog(sysUser, token);
        return LoginRes.builder().token(token).expiredDate(expiredDate).build();
    }

    /**
     * 登录
     * @param username  用户名
     * @param password  密码
     */
    public LoginRes login(String username, String password) {
        //使用token的登录方式
        Optional<SysUser> sysUserOptional = sysUserRepository.findOne(qSysUser.username.eq(username));
        if (!sysUserOptional.isPresent()) {
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "用户名不存在");
        }
        SysUser sysUser = sysUserOptional.get();
        boolean matches = bCryptPasswordEncoder.matches(password, sysUser.getPassword());
        if (!password.equals(sysUser.getPassword()) && !matches) {
            log.info("登陆失败：用户{}密码错误!", username);
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "密码错误");
        }
        if (sysUser.getAccountStatus().equals(SysUser.AccountStatus.LOCKED)) {
            throw new ErrorMsgException("账户被锁定");
        }
        // token有效期 一天
        DateTime expiredDate = DateUtil.offsetDay(new Date(), 1);
        String token = generateToken(sysUser, expiredDate, LoginWay.USERNAME_AND_PASSWORD);
        loginLog(sysUser, token);
        return LoginRes.builder().token(token).expiredDate(expiredDate).build();
    }

    /**
     * 记录登录日志
     * @param sysUser
     */
    private void loginLog(SysUser sysUser, String token){
        JwtPayload payload = jwtTokenService.getPayload(token);
        GeneralAuthentication authentication = GeneralAuthentication.getFromToken(payload);
        authentication.setUser(sysUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserLoginEvent loginEvent = new UserLoginEvent.Builder().user(sysUser).build();
        ac.publishEvent(loginEvent);
    }

    /**
     * 生成token
     * @param sysUser
     * @return
     */
    private String generateToken(SysUser sysUser, Date expiredDate, LoginWay loginWay) {
        JwtPayload jwtPayload = JwtPayload.builder()
                .userId(sysUser.getId())
                .userClass(SysUser.class)
                .loginDevice(LoginDevice.PC_WEB)
                .loginWay(loginWay)
                .loginTime(new Date())
                .build();
        assert sysUser.getId() != null;
        return tokenService.generateToken(jwtPayload, sysUser.getId(), expiredDate);
    }

    /**
     * 获取该用户拥有的权限
     *
     * @param sysUser
     * @return
     */
    public Set<Business> getPermissions(SysUser sysUser) {
        Set<Business> permissions = new HashSet<>();
        if (Objects.nonNull(sysUser.getRoles())) {
            return sysUser.getRoles().stream()
                    .flatMap(sysRole -> sysRole.getBusiness().stream()).collect(Collectors.toSet());
        }
        return permissions;
    }

    /**
     * 重置用户密码为默认密码
     * @param sysUser
     */
    public String resetPassword(SysUser sysUser){
        if (sysUser.isSuperAdmin()) {
            throw new ResponseCodeException(ResponseCode.Common.CAN_NOT_OPERATE_SUPER_ADMIN );
        }
        String newPassword = passwordGeneratorForReset.apply(sysUser);
        sysUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
        sysUserRepository.save(sysUser);
        return newPassword;
    }

    /**
     * 修改用户密码
     * @param user  待修改用户
     * @param oldPassword   旧密码
     * @param newPassword   新密码
     */
    public void modifyPassword(SysUser user, String oldPassword, String newPassword) {
        boolean b = bCryptPasswordEncoder.matches(oldPassword, user.getPassword());
        if (!b) {
            throw new ResponseCodeException(ResponseCode.Common.OLD_PASSWORD_IS_NOT_VALID);
        }
        if (!isPasswordValid(newPassword)){
            throw new ErrorMsgException("密码太简单了");
        }
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        sysUserRepository.save(user);
    }

    Pattern patternForLetter = Pattern.compile("\\w");
    Pattern patternForNumber = Pattern.compile("\\d");

    /**
     * 判断密码是否符合规范
     * @param password
     * @return
     */
    public boolean isPasswordValid(String password){
        assert password != null;
        assert password.length() >= 6;
        //密码安全性要求对生产环境更高
        if (projectConfiguration.isProduction()) {
            assert patternForLetter.matcher(password).find();
            assert patternForNumber.matcher(password).find();
        }
        return true;
    }

}
