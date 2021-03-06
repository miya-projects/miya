package com.miya.system.module.user;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.miya.common.auth.way.GeneralAuthentication;
import com.miya.common.auth.way.LoginDevice;
import com.miya.common.auth.way.LoginWay;
import com.miya.common.config.web.jwt.JwtPayload;
import com.miya.common.config.web.jwt.TokenService;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.common.module.base.BaseService;
import com.miya.common.module.cache.KeyValueStore;
import com.miya.common.module.init.SystemInit;
import com.miya.common.module.init.SystemInitErrorException;
import com.miya.common.module.sms.CacheKeys;
import com.miya.common.service.JwtTokenService;
import com.miya.system.config.ProjectConfiguration;
import com.miya.system.config.business.Business;
import com.miya.system.module.download.DownloadService;
import com.miya.system.module.user.event.UserLoginEvent;
import com.miya.system.module.user.event.UserModifyEvent;
import com.miya.system.module.user.model.*;
import com.querydsl.core.types.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.data.util.CastUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ????????????
 */
@Slf4j
@Transactional
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

    private static final QSysUser qSysUser = QSysUser.sysUser;

    /**
     * ?????????????????????
     */
    Function<SysUserForm, String> defaultPasswordGenerator = u -> "123456";

    /**
     * ?????????????????????????????????
     */
    Function<SysUser, String> passwordGeneratorForReset = u -> "123456";


    @PostConstruct
    public void customize(){
        this.defaultPasswordGenerator = customizer.passwordGeneratorForNewUser();
        this.passwordGeneratorForReset = customizer.passwordGeneratorForReset();
    }

    /**
     * ????????????
     * @param userForm
     */
    public R<?> save(SysUserForm userForm) {
        return saveBySocial(userForm, CastUtils.cast(Collections.EMPTY_LIST));
    }

    /**
     * ????????????
     * @param userForm
     * @param socials ????????????
     */
    public R<?> saveBySocial(SysUserForm userForm, List<SysUserSocial> socials) {
        boolean exists = sysUserRepository.exists(qSysUser.username.eq(userForm.getUsername()));
        if (exists) {
            return R.errorWithMsg("?????????????????????");
        }
        boolean existsSamePhone = sysUserRepository.exists(qSysUser.phone.eq(userForm.getPhone()));
        if (existsSamePhone){
            return R.errorWithMsg("???????????????");
        }
        SysUser sysUser = userForm.mergeToNewPo();
        sysUser.setPassword(passwordEncoder.encode(this.defaultPasswordGenerator.apply(userForm)));
        sysUser.setSysUserSocials(new HashSet<>(socials));
        sysUserRepository.save(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.NEW));
        return R.success();
    }

    /**
     * ??????????????????????????????
     * @param userModifyForm
     * @param user
     */
    public void modifyProfile(SysUserModifyForm userModifyForm, SysUser user) {
        userModifyForm.mergeToPo(user);
        sysUserRepository.save(user);
        ac.publishEvent(new UserModifyEvent(user, UserModifyEvent.UserModifyType.MODIFY_USERINFO));
    }

    /**
     * ????????????????????????
     * @param user
     */
    public void update(SysUser user) {
        sysUserRepository.save(user);
        ac.publishEvent(new UserModifyEvent(user, UserModifyEvent.UserModifyType.MODIFY_USERINFO));
    }

    /**
     * ??????????????????
     * @param sysUserForm
     * @param user
     */
    public void update(SysUserForm sysUserForm, SysUser user) {
        sysUserForm.mergeToPo(user);
        boolean existsSamePhone = sysUserRepository.exists(qSysUser.phone.eq(user.getPhone()).and(qSysUser.id.ne(user.getId())));
        if (existsSamePhone){
            throw new ErrorMsgException("???????????????");
        }
        boolean existsSameUsername = sysUserRepository.exists(qSysUser.phone.eq(user.getPhone()).and(qSysUser.id.ne(user.getId())));
        if (existsSameUsername){
            throw new ErrorMsgException("???????????????");
        }
        sysUserRepository.save(user);
        ac.publishEvent(new UserModifyEvent(user, UserModifyEvent.UserModifyType.MODIFY_USERINFO));
    }

    /**
     * ????????????
     * @param sysUser
     */
    public void delete(SysUser sysUser) {
        // todo ?????????????????? ??????????????????
        if (sysUser.isSuperAdmin()) {
            throw new ResponseCodeException(ResponseCode.Common.CAN_NOT_OPERATE_SUPER_ADMIN);
        }
        sysUserRepository.delete(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.DELETE));
    }

    /**
     * ????????????
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
     * ??????
     * @param sysUser
     */
    public R<?> unFreeze(SysUser sysUser) {
        sysUser.setAccountStatus(SysUser.AccountStatus.NORMAL);
        sysUserRepository.save(sysUser);
        ac.publishEvent(new UserModifyEvent(sysUser, UserModifyEvent.UserModifyType.UNFREEZE));
        return R.success();
    }


    /**
     * ?????????????????????dto
     */
    @Setter
    @Getter
    @Builder
    public static class LoginRes {
        private String token;
        /**
         * token????????????
         */
        private Date expiredDate;
//        private SysUserDTO sysUserDTO;
    }

    /**
     * ???????????????+???????????????
     * @param phone  ?????????
     * @param verifyCode  ?????????
     */
    public LoginRes loginByPhone(String phone, String verifyCode) {
        Optional<SysUser> sysUserOptional = sysUserRepository.findOne(qSysUser.phone.eq(phone));
        if (!sysUserOptional.isPresent()) {
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "???????????????");
        }
        SysUser sysUser = sysUserOptional.get();
        String rightVerifyCode = keyValueStore.get(CacheKeys.PHONE_VERIFY.toCacheKey(phone));
        if ((verifyCode != null) && (!StrUtil.equals(rightVerifyCode, verifyCode))){
            log.debug("?????????????????????{}???????????????!", phone);
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "???????????????");
        }
        if (sysUser.getAccountStatus().equals(SysUser.AccountStatus.LOCKED)) {
            throw new ErrorMsgException("???????????????");
        }
        // token????????? ??????
        DateTime expiredDate = DateUtil.offsetDay(new Date(), 1);
        String token = generateToken(sysUser, expiredDate, LoginWay.PHONE_AND_CODE);
        loginLog(sysUser, token);
        return LoginRes.builder().token(token).expiredDate(expiredDate).build();
    }

    /**
     * ??????
     * @param username  ?????????
     * @param password  ??????
     */
    public LoginRes login(String username, String password) {
        //??????token???????????????
        Optional<SysUser> sysUserOptional = sysUserRepository.findOne(qSysUser.username.eq(username));
        if (!sysUserOptional.isPresent()) {
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "??????????????????");
        }
        SysUser sysUser = sysUserOptional.get();
        boolean matches = passwordEncoder.matches(password, sysUser.getPassword());
        if (!password.equals(sysUser.getPassword()) && !matches) {
            log.info("?????????????????????{}????????????!", username);
            throw new ResponseCodeException(ResponseCode.Common.LOGIN_FAILED, "????????????");
        }
        if (sysUser.getAccountStatus().equals(SysUser.AccountStatus.LOCKED)) {
            throw new ErrorMsgException("???????????????");
        }
        // token????????? ??????
        DateTime expiredDate = DateUtil.offsetDay(new Date(), 1);
        String token = generateToken(sysUser, expiredDate, LoginWay.USERNAME_AND_PASSWORD);
        loginLog(sysUser, token);
        return LoginRes.builder().token(token).expiredDate(expiredDate).build();
    }

    /**
     * ??????????????????
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
     * ??????token
     * @param sysUser
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
     * ??????????????????????????????
     *
     * @param sysUser
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
     * ?????????????????????????????????
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
     * ??????????????????
     * @param user  ???????????????
     * @param oldPassword   ?????????
     * @param newPassword   ?????????
     */
    public void modifyPassword(SysUser user, String oldPassword, String newPassword) {
        boolean b = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!b) {
            throw new ResponseCodeException(ResponseCode.Common.OLD_PASSWORD_IS_NOT_VALID);
        }
        if (!isPasswordValid(newPassword)){
            throw new ErrorMsgException("??????????????????");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
    }

    Pattern patternForLetter = Pattern.compile("\\w");
    Pattern patternForNumber = Pattern.compile("\\d");

    /**
     * ??????????????????????????????
     * @param password
     */
    public boolean isPasswordValid(String password){
        assert password != null;
        assert password.length() >= 6;
        //??????????????????????????????????????????
        if (projectConfiguration.isProduction()) {
            assert patternForLetter.matcher(password).find();
            assert patternForNumber.matcher(password).find();
        }
        return true;
    }


    @Override
    public void init() throws SystemInitErrorException {
        // ????????????
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
     * ??????Excel
     * @param predicate ????????????
     * @param response http??????
     */
    public void export(Predicate predicate, HttpServletResponse response) throws IOException {
        URL resource = ResourceUtil.getResource("excel-export-template/user.xlsx");
        String fileName = "????????????.xlsx";
        Iterable<SysUser> users = sysUserRepository.findAll(predicate);
        ArrayList<SysUser> userList = ListUtil.toList(users);
        Context context = new Context();
        context.putVar("items", userList);
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
        response.setContentType("application/octet-stream");
        JxlsHelper.getInstance().processTemplate(resource.openStream(), response.getOutputStream(), context);
        downloadService.generateTask("??????????????????", fileName);
    }

}
