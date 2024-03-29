package com.miya.system.module.log;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.util.AuthenticationUtil;
import com.miya.system.config.business.Business;
import com.miya.system.module.role.SysRoleService;
import com.miya.system.module.user.model.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 日志服务
 */
@Service
@RequiredArgsConstructor
public class LogService {

    private final SysLogRepository sysLogRepository;

    /**
     * 增加一条日志，所属业务模块，操作人等信息自动识别
     * @param content 日志内容
     */
    public void log(String content){
        log(content, null,null, null);
    }

    /**
     * 增加一条日志
     * @param content   日志内容
     * @param operationType 操作类型
     */
    public void log(String content, String operationType){
        log(content, operationType, null, null);
    }

    /**
     * 日志内容
     * @param content   日志内容
     * @param operationType 操作类型
     * @param businessId    相关业务数据的id
     */
    public void log(String content, String operationType, String businessId){
        log(content, operationType, businessId, null);
    }

    /**
     * 日志内容
     * @param content   日志内容
     * @param operationType 操作类型
     * @param businessId    相关业务数据的id
     * @param extra         其他额外数据
     */
    public void log(String content, String operationType, String businessId, Map<String, Object> extra){
        log(getBusiness().map(Business::getName).orElse(null), content, operationType, businessId, extra);
    }

    /**
     * 创建日志对象并保存日志
     * @param business 所属业务模块
     * @param content   日志内容
     * @param operationType 操作类型
     * @param businessId    相关业务数据的id
     * @param extra 其他额外数据
     */
    public void log(String business, String content, String operationType, String businessId, Map<String, Object> extra){
        SysLog sysLog = createSysLog(business, content, operationType, businessId, extra);
        sysLogRepository.save(sysLog);
    }

    /**
     * 创建日志对象并保存日志
     * @param business 所属业务模块
     * @param content   日志内容
     * @param operationType 操作类型
     * @param businessId    相关业务数据的id
     * @param extra 其他额外数据
     * @param operatorName  操作人
     */
    public void log(String business, String content, String operationType, String businessId, Map<String, Object> extra, String operatorName){
        SysLog sysLog = createSysLog(business, content, operationType, businessId, extra, operatorName);
        sysLogRepository.save(sysLog);
    }

    /**
     * 创建日志对象
     * @param content       所属业务模块
     * @param operationType 日志内容
     * @param businessId    相关业务数据的id
     * @param extra         其他额外数据
     * @return              日志对象
     */
    private SysLog createSysLog(String business, String content, String operationType, String businessId, Map<String, Object> extra){
        Object principal = AuthenticationUtil.getPrincipal();
        String name = this.getUserName(principal);
        return createSysLog(business, content, operationType, businessId, extra, name);
    }

    /**
     * 创建日志对象
     * @param content       所属业务模块
     * @param operationType 日志内容
     * @param businessId    相关业务数据的id
     * @param extra         其他额外数据
     * @param operatorName  操作人
     * @return              日志对象
     */
    private SysLog createSysLog(String business, String content, String operationType, String businessId, Map<String, Object> extra, String operatorName){
        SysLog sysLog = new SysLog();
        sysLog.setBusiness(business);
        sysLog.setContent(content);
        sysLog.setOperatorName(operatorName);
        sysLog.setOperationType(operationType);
        sysLog.setExtra(extra);
        sysLog.setBusinessId(businessId);
        return sysLog;
    }

    /**
     * 根据当前web环境推断business
     * {@link com.miya.system.config.filter.interceptors.ApiAccessInterceptor}
     * @return  功能定义
     */
    private Optional<Business> getBusiness(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(servletRequestAttributes)){
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object businessAttr = request.getAttribute("business");
            return Optional.ofNullable(SpringUtil.getBean(SysRoleService.class)
                    .valueOfCode(Optional.ofNullable(businessAttr).map(Object::toString).orElse("")));
        }
        return Optional.empty();
    }

    /**
     * 获取姓名，用于记录日志
     * @param principal 被验证身份的主体
     * @return  用户姓名
     */
    private String getUserName(Object principal){
        if (principal == null){
            return null;
        }
        if (principal instanceof SysUser){
            return ((SysUser)principal).getName();
        }
        Object name = ReflectUtil.getFieldValue(principal, "name");
        if (name == null){
            return null;
        }
        return name.toString();
    }

}
