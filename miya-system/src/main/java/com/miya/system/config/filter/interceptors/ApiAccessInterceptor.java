package com.miya.system.config.filter.interceptors;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.miya.common.annotation.Acl;
import com.miya.system.config.business.Business;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.system.module.role.SysRoleService;
import com.miya.system.module.user.SysUserService;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.user.model.SysUserPrincipal;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * @author 杨超辉
 * api接口权限拦截器
 */
@Slf4j
@Component
public class ApiAccessInterceptor implements HandlerInterceptor {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysRoleService sysRoleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Object bean = handlerMethod.getBean();
        Object principal = request.getAttribute("principal");
        // 先检测class
        Acl aclForClass = AnnotationUtils.findAnnotation(bean.getClass(), Acl.class);
        Acl aclForMethod = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Acl.class);
        Class<?> userType = null;
        String[] businessCodes = null;
        if (Objects.nonNull(aclForClass)){
            userType = aclForClass.userType();
            businessCodes = aclForClass.business();
        }
        if (Objects.nonNull(aclForMethod)){
            userType = aclForMethod.userType();
            if (aclForMethod.business().length > 0){
                businessCodes = aclForMethod.business();
            }
        }
        if (Acl.NotNeedLogin.class.equals(userType) || userType == null){
            return true;
        }
        if (Objects.isNull(principal)) {
            response.setStatus(401);
            response.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.toString());
            response.getWriter().write(JSONUtil.toJsonStr(R.errorWithCodeAndMsg(ResponseCode.Common.NO_LOGIN)));
            return false;
        }
        if (!Acl.AllUser.class.equals(userType)) {
            if (!principal.getClass().equals(userType)) {
                // todo 预警入库 模块化
                log.warn("用户类型与接口允许类型不一致! 用户: {} 方法: {}", principal.getClass().toString(), handlerMethod.getMethod().toString());
                reject(response);
                return false;
            }
        }
        if (principal instanceof SysUserPrincipal) {
            SysUserPrincipal userPrincipal = (SysUserPrincipal) principal;
            if (Objects.nonNull(businessCodes) && businessCodes.length > 0) {
                request.setAttribute("business", businessCodes[0]);
            }
            /*
             * 超级管理员不受所有权限控制
             */
            if (userPrincipal.isSuperAdmin()) {
                return true;
            }
            if (Objects.nonNull(businessCodes) && businessCodes.length > 0) {
                Set<Business> permissions = sysUserService.getPermissions(userPrincipal);
                for (Business b : permissions) {
                    for (String code : businessCodes) {
                        Business business = sysRoleService.valueOfCode(code);
                        if (b.equals(business)) {
                            return true;
                        }
                    }
                }
                response.setStatus(200);
                String requestURI = request.getRequestURI();
                log.info("拒绝 {} 访问 {} :无权限",userPrincipal.getName(),requestURI);
                reject(response);
                return false;
            }
            return true;
        }
        return true;
    }

    @SneakyThrows
    private void reject(HttpServletResponse response) {
        response.setStatus(403);
        response.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.toString());
        response.getWriter().write(JSONUtil.toJsonStr(R.errorWithCodeAndMsg(ResponseCode.Common.NO_PERMISSION)));
    }

}
