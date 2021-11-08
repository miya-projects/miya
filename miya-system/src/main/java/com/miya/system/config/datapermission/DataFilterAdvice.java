package com.miya.system.config.datapermission;

import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.role.SysDefaultRoles;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 数据权限过滤
 */
@Component
@Aspect
@Slf4j
public class DataFilterAdvice {

    @PersistenceContext
    private EntityManager entityManager;


    @Pointcut("@annotation(com.miya.system.config.datapermission.DataFilter)")
    private void dataFilterPointcut() {}

    @Pointcut("@annotation(com.miya.system.config.datapermission.DataFilters)")
    private void dataFiltersPointcut() {}

    @Around(value = "dataFilterPointcut() && @annotation(dataFilter)")
    public Object doProcess(ProceedingJoinPoint joinPoint, DataFilter dataFilter) throws Throwable {
        processDataFilter(dataFilter);
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error(e.toString());
        } finally {
            entityManager.unwrap(Session.class).disableFilter(dataFilter.filter());
        }
        return null;
    }

    @Around(value = "dataFiltersPointcut() && @annotation(dataFilters)")
    public Object doProcess(ProceedingJoinPoint joinPoint, DataFilters dataFilters) {
        for (DataFilter dataFilter : dataFilters.value()) {
            processDataFilter(dataFilter);
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error(e.toString());
        } finally {
            Session session = entityManager.unwrap(Session.class);
            for (DataFilter dataFilter : dataFilters.value()) {
                session.disableFilter(dataFilter.filter());
            }
        }
        return null;
    }

    /**
     * 应用dataFilter到session上
     * @param dataFilter
     */
    private void processDataFilter(DataFilter dataFilter) {
        Session session = entityManager.unwrap(Session.class);
        String userId = getCurrentUserId();
        Set<SysRole> currentUserRoles = getCurrentUserRoles();

        String filterName = dataFilter.filter();
        List<SysDefaultRoles> noCheckRoles = new ArrayList<>(Arrays.asList(dataFilter.noCheckRoles()));
        noCheckRoles.add(SysDefaultRoles.ADMIN);

        boolean noCheck = noCheckRoles.stream().map(SysDefaultRoles::getId)
                .anyMatch(id -> currentUserRoles.stream().map(SysRole::getId).anyMatch(roleId -> roleId.equals(id)));
        if (noCheck) {
            return;
        }
        Filter followerFilter = session.enableFilter(filterName);
        followerFilter.setParameterList("userId", Collections.singletonList(userId));
    }

    /**
     * 获取当前登录用户id
     *
     * @return
     */
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = Optional.ofNullable(authentication).map(Authentication::getPrincipal).orElse(null);
        if (principal instanceof SysUser) {
            return ((SysUser) principal).getId();
        }
        log.info("匿名用户访问带数据权限接口，可能有bug");
        return null;
    }

    /**
     * 获取当前登录用户角色
     * @return
     */
    public Set<SysRole> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = Optional.ofNullable(authentication).map(Authentication::getPrincipal).orElse(null);
        if (principal instanceof SysUser) {
            return new HashSet<>(((SysUser) principal).getRoles());
        }
        return new HashSet<>();
    }

}
