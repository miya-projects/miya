package com.miya.system.module.role;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.miya.common.exception.ObjectNotExistException;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseService;
import com.miya.common.module.init.SystemInit;
import com.miya.system.config.business.Business;
import com.miya.system.config.business.SystemErrorCode;
import com.miya.system.module.role.event.RoleModifyEvent;
import com.miya.system.module.role.model.QSysRole;
import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.role.model.SysRoleForm;
import com.miya.system.module.user.MiyaSystemUserConfig;
import com.miya.common.util.JSONUtils;
import com.miya.system.module.user.SysUserRepository;
import com.miya.system.module.user.model.QSysUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 角色服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysRoleService extends BaseService implements SystemInit {

    /**
     * business功能文件默认查找路径
     */
    public static final List<String> DEFAULT_BUSINESS_LOCATION = Arrays.asList("config/business.json", "business.json",
            "business/*.json", "config/business/*.json");

    private final MiyaSystemUserConfig miyaSystemUserConfig;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRepository sysUserRepository;

    @Getter
    private List<Business> business;

    @SneakyThrows
    @PostConstruct
    public void load(){
        // 寻找功能定义文件并生成business对象
        final ArrayList<URL> resources = new ArrayList<>();
        List<String> locations = new ArrayList<>(DEFAULT_BUSINESS_LOCATION);
        Optional.ofNullable(miyaSystemUserConfig.businessFileName()).ifPresent(locations::addAll);

        for (String url : locations) {
            Enumeration<URL> res = this.getClass().getClassLoader().getResources(url);
            resources.addAll(Collections.list(res));
        }

        HashSet<Business> allBusinesses = new HashSet<>();
        for (URL url : resources) {
            String json = IoUtil.read(url.openStream(), Charset.defaultCharset());
            final List<Business> businesses = JSONUtils.toJavaObject(json, new TypeReference<List<Business>>() {
            });
            assert businesses != null;
            allBusinesses.addAll(businesses);
        }
        //检测重复code警告
        this.business = new ArrayList<>(allBusinesses);
        for (Business business : this.business) {
            fillParent(business, business.getChildren());
        }
    }

    @Override
    public void init() {
        // 检查默认角色有效性
        for (SysDefaultRoles value : SysDefaultRoles.values()) {
            touchSystemRole(value);
        }
        // Arrays.stream(SysDefaultRoles.values()).forEach(this::touchSystemRole);
    }

    /**
     * 摸一下系统角色，如果不存在就创建
     * @param defaultRole
     */
    public void touchSystemRole(DefaultRole defaultRole) {
        try {
            defaultRole.getSysRole();
        } catch (ObjectNotExistException e){
            log.info("默认角色【{}】不存在，自动创建...", defaultRole.getName());
            // 没有就新增
            SysRole role = new SysRole();
            role.setIsSystem(true);
            role.setName(defaultRole.getName());
            role.setId(defaultRole.getId());
            sysRoleRepository.save(role);
        }
    }

    /**
     * 填充父节点
     * @param parent
     * @param children
     */
    private void fillParent(Business parent, List<Business> children) {
        if (children == null) {
            return;
        }
        for (Business child : children) {
            child.setParent(parent);
            fillParent(child, child.getChildren());
        }
    }

    /**
     * 根据fullCode获取business对象
     * @param fullCode
     */
    public Business valueOfCode(String fullCode){
        return valueOfCode(fullCode, business);
    }

    /**
     * 根据code返回Business
     * @param fullCode
     */
    public Business valueOfCode(String fullCode, List<Business> business){
        String[] businessCodes = fullCode.split(":");
        Business bus = null;
        List<Business> lookFor = business;
        for (String code : businessCodes) {
            bus = findBusinessByCode(code, lookFor);
            if (bus != null){
                lookFor = bus.getChildren();
            }
        }
        return bus;
    }

    /**
     * 为角色设置权限
     * @param sysRole   角色对象
     * @param codes     为该角色设置的full code数组
     */
    public void saveBusiness(SysRole sysRole, String[] codes) {
        if (codes == null) {
            codes = new String[0];
        }
        sysRole.getPermissions().clear();
        for (String code : codes) {
            //校验code
            Business business = valueOfCode(code);
            assert business != null;
            sysRole.getPermissions().add(code);
        }
        sysRoleRepository.save(sysRole);
        this.ac.publishEvent(new RoleModifyEvent(sysRole, RoleModifyEvent.RoleModifyType.MODIFY_PERMISSION));
    }


    /**
     * 通过id查询系统默认角色，且有缓存 该缓存比较特殊，启动后一般不需要evict。
     * @param id    角色id
     * @return      角色对象
     */
    @Cacheable(cacheNames = "SYS_ROLE_DEFAULT_ROLE", key = "#id")
    public SysRole getDefaultRoleById(String id) {
        QSysRole qSysRole = QSysRole.sysRole;
        Optional<SysRole> roleOptional = sysRoleRepository.findOne(qSysRole.isSystem.isTrue().and(qSysRole.id.eq(id)));
        if (roleOptional.isPresent()) {
            return roleOptional.get();
        }
        throw new ObjectNotExistException("默认角色[" +  id + "] 在数据库不存在");
    }

    /**
     * 修改角色
     * @param sysRoleForm   修改后的角色信息
     * @param sysRole       待修改角色
     */
    public R<?> updateRole(SysRoleForm sysRoleForm, SysRole sysRole) {
        if (sysRole.getIsSystem()){
            return R.errorWithCodeAndMsg(SystemErrorCode.OPE_SYSTEM_ROLE);
        }
        sysRoleForm.setName(sysRoleForm.getName().trim());
        long count = sysRoleRepository.count(QSysRole.sysRole.name.eq(sysRoleForm.getName()));
        if (count > 0 && !sysRoleForm.getName().equals(sysRole.getName())){
            return R.errorWithMsg("角色名已被使用");
        }
        BeanUtil.copyProperties(sysRoleForm.mergeToNewPo(), sysRole, CopyOptions.create().ignoreNullValue());
        sysRoleRepository.save(sysRole);
        this.ac.publishEvent(new RoleModifyEvent(sysRole, RoleModifyEvent.RoleModifyType.MODIFY));
        return R.success();
    }

    /**
     * 新增角色
     * @param sysRoleForm
     */
    public R<?> saveRole(SysRoleForm sysRoleForm) {
        SysRole sysRole = sysRoleForm.mergeToNewPo();
        sysRole.setIsSystem(false);
        sysRoleRepository.save(sysRole);
        this.ac.publishEvent(new RoleModifyEvent(sysRole, RoleModifyEvent.RoleModifyType.NEW));
        return R.success();
    }

    /**
     * 删除角色
     * @param sysRole 待删除角色
     */
    public R<?> deleteRole(SysRole sysRole) {
        boolean exists = sysUserRepository.exists(QSysUser.sysUser.roles.any().eq(sysRole));
        if (exists) {
            return R.errorWithMsg("该角色下有用户，不可删除");
        }
        if (sysRole.getIsSystem()){
            return R.errorWithCodeAndMsg(SystemErrorCode.OPE_SYSTEM_ROLE);
        }
        sysRoleRepository.delete(sysRole);
        this.ac.publishEvent(new RoleModifyEvent(sysRole, RoleModifyEvent.RoleModifyType.DELETE));
        return R.success();
    }

    /**
     * 根据sortCode寻找business
     * @param shortCode
     */
    private Business findBusinessByCode(String shortCode, List<Business> business){
        Optional<Business> first = business.stream().filter(bus -> bus.getCode().equals(shortCode)).findFirst();
        return first.orElse(null);
    }

}
