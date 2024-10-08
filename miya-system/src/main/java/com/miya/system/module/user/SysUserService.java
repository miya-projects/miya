package com.miya.system.module.user;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.auth.way.GeneralAuthentication;
import com.miya.common.auth.way.LoginDevice;
import com.miya.common.auth.way.LoginWay;
import com.miya.common.config.web.jwt.JwtPayload;
import com.miya.common.config.web.jwt.TokenService;
import com.miya.common.config.xlsx.XlsxUtil;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.base.BaseService;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.common.module.config.SysConfigService;
import com.miya.common.module.init.SystemInit;
import com.miya.common.module.init.SystemInitErrorException;
import com.miya.common.service.JwtTokenService;
import com.miya.common.util.TransactionUtil;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.business.Business;
import com.miya.system.module.download.DownloadService;
import com.miya.system.module.download.SimpleDownloadTask;
import com.miya.system.module.notice.SysNoticeService;
import com.miya.system.module.user.config.MiyaSystemUserConfig;
import com.miya.system.module.user.dto.*;
import com.miya.system.module.user.event.UserLoginEvent;
import com.miya.system.module.user.event.UserModifyEvent;
import com.miya.system.module.user.model.*;
import com.miya.third.sms.CacheKeys;
import com.querydsl.core.types.Predicate;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.CastUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService extends BaseService implements SystemInit {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final KeyValueStore keyValueStore;
    private final ProjectConfiguration projectConfiguration;
    private final JwtTokenService jwtTokenService;
    private final MiyaSystemUserConfig customizer;
    private final DownloadService downloadService;
    private final SysConfigService configService;
    private final SysNoticeService sysNoticeService;

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
     * 新增用户
     * @param userForm
     */
    public R<?> save(SysUserForm userForm) {
        return SpringUtil.getBean(SysUserService.class).saveBySocial(userForm, CastUtils.cast(Collections.EMPTY_LIST));
    }

    /**
     * 新增用户
     * @param userForm
     * @param socials 社交媒体
     */
    public R<?> saveBySocial(SysUserForm userForm, List<SysUserSocial> socials) {
        boolean exists = sysUserRepository.exists(qSysUser.username.eq(userForm.getUsername()));
        if (exists) {
            return R.errorWithMsg("用户名已被注册");
        }
        boolean existsSamePhone = sysUserRepository.exists(qSysUser.phone.eq(userForm.getPhone()));
        if (existsSamePhone){
            return R.errorWithMsg("手机号重复");
        }
        SysUser sysUser = userForm.mergeToNewPo();
        sysUser.setPassword(passwordEncoder.encode(this.defaultPasswordGenerator.apply(userForm)));
        sysUser.setSysUserSocials(new HashSet<>(socials));
        sysUserRepository.save(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.NEW));
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
        ac.publishEvent(new UserModifyEvent(user, UserModifyEvent.UserModifyType.MODIFY_USERINFO));
    }

    /**
     * 修改当前用户信息
     * @param user
     */
    public void update(SysUser user) {
        sysUserRepository.save(user);
        ac.publishEvent(new UserModifyEvent(user, UserModifyEvent.UserModifyType.MODIFY_USERINFO));
    }

    /**
     * 修改用户信息
     * @param sysUserForm
     * @param user
     */
    public void update(SysUserForm sysUserForm, SysUser user) {
        boolean existsSamePhone = sysUserRepository.exists(qSysUser.phone.eq(sysUserForm.getPhone()).and(qSysUser.id.ne(user.getId())));
        if (existsSamePhone){
            throw new ErrorMsgException("手机号重复");
        }
        boolean existsSameUsername = sysUserRepository.exists(qSysUser.username.eq(sysUserForm.getUsername()).and(qSysUser.id.ne(user.getId())));
        if (existsSameUsername){
            throw new ErrorMsgException("用户名重复");
        }
        // mergeToPo放到后面，因为合并持久态对象后，再进行查询会先触发commit
        sysUserForm.mergeToPo(user);
        sysUserRepository.save(user);
        ac.publishEvent(new UserModifyEvent(user, UserModifyEvent.UserModifyType.MODIFY_USERINFO));
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
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.DELETE));
    }

    /**
     * 冻结用户
     * @param sysUser
     */
    public R<?> freeze(SysUser sysUser) {
        if (sysUser.isSuperAdmin()) {
            return R.errorWithCodeAndMsg(ResponseCode.Common.CAN_NOT_OPERATE_SUPER_ADMIN);
        }
        sysUser.setAccountStatus(SysUser.AccountStatus.LOCKED);
        sysUserRepository.save(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.FREEZE));
        return R.success();
    }

    /**
     * 解冻
     * @param sysUser
     */
    public R<?> unFreeze(SysUser sysUser) {
        sysUser.setAccountStatus(SysUser.AccountStatus.NORMAL);
        sysUserRepository.save(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.UNFREEZE));
        return R.success();
    }

    /**
     * 以他人的身份登录
     * @param sysUser   被登录的用户
     * @return  token
     */
    public LoginDTO loginAs(SysUser sysUser) {
        // token有效期 30分钟
        DateTime expiredDate = DateUtil.offsetMinute(new Date(), 30);
        String token = generateToken(sysUser, expiredDate, LoginWay.USERNAME_AND_PASSWORD);
        return LoginDTO.builder().token(token).expiredDate(expiredDate).build();
    }

    public CurrentSysUserDTO current(SysUserPrincipal sysUserPrincipal) {
        CurrentSysUserDTO dto = new CurrentSysUserDTO();
        assert sysUserPrincipal.getId() != null;
        dto.setUser(SysUserDetailDTO.of(sysUserRepository.getReferenceById(sysUserPrincipal.getId())));
        dto.setSystemMeta(configService.getSystemMeta());
        dto.setUnreadNoticeAmount(sysNoticeService.unreadNoticeAmount(sysUserPrincipal.getId()));
        return dto;
    }

    /**
     * 通过手机号+验证码登录
     * @param phone  手机号
     * @param verifyCode  验证码
     */
    public LoginDTO loginByPhone(String phone, String verifyCode) {
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
        DateTime expiredDate = DateUtil.offsetDay(new Date(), 7);
        String token = generateToken(sysUser, expiredDate, LoginWay.PHONE_AND_CODE);
        loginLog(sysUser, token);
        return LoginDTO.builder().token(token).expiredDate(expiredDate).build();
    }

    /**
     * 登录
     * @param username  用户名
     * @param password  密码
     */
    public LoginDTO login(String username, String password) {
        //使用token的登录方式
        Optional<SysUser> sysUserOptional = sysUserRepository.findOne(qSysUser.username.eq(username));
        if (sysUserOptional.isEmpty()) {
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "用户名不存在");
        }
        SysUser sysUser = sysUserOptional.get();
        boolean matches = passwordEncoder.matches(password, sysUser.getPassword());
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
        return LoginDTO.builder().token(token).expiredDate(expiredDate).build();
    }

    /**
     * 记录登录日志
     * @param sysUser
     */
    private void loginLog(SysUser sysUser, String token){
        JwtPayload payload = jwtTokenService.getPayload(token);
        GeneralAuthentication authentication = GeneralAuthentication.getFromToken(payload);
        authentication.setUser(sysUser);
        UserLoginEvent loginEvent = new UserLoginEvent.Builder().user(sysUser).build();
        ac.publishEvent(loginEvent);
    }

    /**
     * q: 下面这行代码是什么意思？
     * a: 这里是将用户信息转换为jwt的payload，然后生成token
     *
     * 生成token
     * @param sysUser
     */
    private String generateToken(SysUser sysUser, Date expiredDate, LoginWay loginWay) {
        SysUserPrincipal userPrincipal = SysUserPrincipal.of(sysUser);
        JwtPayload jwtPayload = JwtPayload.builder()
                .userId(sysUser.getId())
                .userClass(userPrincipal.getClass())
                .loginDevice(LoginDevice.PC_WEB)
                .loginWay(loginWay)
                .loginTime(new Date())
                .exp(expiredDate)
                .build();
        assert sysUser.getId() != null;
        return tokenService.generateToken(jwtPayload, userPrincipal);
    }

    /**
     * 获取该用户拥有的权限
     *
     * @param sysUser
     */
    public Set<Business> getPermissions(SysUserPrincipal sysUser) {
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
        sysUser.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.RESET_PASSWORD));
        return newPassword;
    }

    /**
     * 修改用户密码
     * @param user  待修改用户
     * @param oldPassword   旧密码
     * @param newPassword   新密码
     */
    public void modifyPassword(SysUser user, String oldPassword, String newPassword) {
        boolean b = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!b) {
            throw new ResponseCodeException(ResponseCode.Common.OLD_PASSWORD_IS_NOT_VALID);
        }
        if (!isPasswordValid(newPassword)){
            throw new ErrorMsgException("密码太简单了");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
    }

    Pattern patternForLetter = Pattern.compile("\\w");
    Pattern patternForNumber = Pattern.compile("\\d");

    /**
     * 判断密码是否符合规范
     * @param password
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


    @Override
    public void init() throws SystemInitErrorException {
        // 超管帐号
        if (sysUserRepository.existsById("1")) {
            return;
        }
        SysUserForm form = new SysUserForm();
        form.setName("Admin");
        form.setUsername("admin");
        form.setPhone("13800000000");
        SysUser sysUser = form.mergeToNewPo();
        sysUser.setPassword(passwordEncoder.encode(this.defaultPasswordGenerator.apply(form)));
        sysUser.setId("1");
        sysUserRepository.save(sysUser);
    }

    /**
     * 导出Excel
     * @param predicate 筛选条件
     * @param response http响应
     */
    public void export(Predicate predicate, HttpServletResponse response, SysUser user) {
        SimpleDownloadTask downloadTask = new SimpleDownloadTask("后台用户数据", "用户.xlsx", user, () -> {
            URL resource = ResourceUtil.getResource("excel-export-template/user.xlsx");
            List<SysUserForExport> userList = TransactionUtil.INSTANCE.transactional(() -> {
                Iterable<SysUser> users = sysUserRepository.findAll(predicate);
                return ListUtil.toList(users).stream().map(SysUserForExport::of).collect(Collectors.toList());
            });
            Map<String, Object> data = new HashMap<>();
            data.put("items", userList);

            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = null;
            try {
                pipedOutputStream = new PipedOutputStream(pipedInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 异步执行
            PipedOutputStream finalPipedOutputStream = pipedOutputStream;
            CompletableFuture.runAsync(() -> {
                try {
                    XlsxUtil.export(resource.openStream(), finalPipedOutputStream, data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return pipedInputStream;
        });
        downloadService.export(downloadTask);
    }

}
