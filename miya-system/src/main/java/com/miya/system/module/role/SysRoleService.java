package com.miya.system.module.role;

import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.miya.common.module.init.SystemInit;
import com.miya.system.config.business.Business;
import com.miya.system.module.role.model.QSysRole;
import com.miya.system.module.role.model.SysRole;
import com.miya.system.module.user.SysUserCustomizer;
import com.miya.common.util.JSONUtils;
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
public class SysRoleService implements SystemInit {

    /**
     * business功能文件默认查找路径
     */
    public static final List<String> DEFAULT_BUSINESS_LOCATION = Arrays.asList("config/business.json", "business.json",
            "business/*.json", "config/business/*.json");

    private final SysUserCustomizer customizer;
    private final SysRoleRepository sysRoleRepository;

    @Getter
    private List<Business> business;

    @SneakyThrows
    @PostConstruct
    public void load(){
        // 寻找功能定义文件并生成business对象
        final ArrayList<URL> resources = new ArrayList<>();
        List<String> locations = new ArrayList<>(DEFAULT_BUSINESS_LOCATION);
        Optional.ofNullable(customizer.businessFileName()).ifPresent(locations::addAll);

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
    public void init(){
        // 检查默认角色有效性
        for (SysDefaultRoles value : SysDefaultRoles.values()) {
            try{
                value.getSysRole();
            }catch (Exception e){
                // 没有就新增
                SysRole role = new SysRole();
                role.setIsSystem(true);
                role.setName(value.getName());
                role.setId(value.getId());
                sysRoleRepository.save(role);
            }
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
     * @return
     */
    public Business valueOfCode(String fullCode){
        return valueOfCode(fullCode, business);
    }

    /**
     * 根据code返回Business
     * @param fullCode
     * @return
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
    }


    /**
     * 通过id查询系统默认角色，且有缓存 该缓存比较特殊，启动后一般不需要evict。
     * @param id    角色id
     * @return      角色对象
     */
    @Cacheable(cacheNames = "SYS_ROLE_DEFAULT_ROLE", key = "#id")
    public SysRole getDefaultRoleById(String id){
        Optional<SysRole> roleOptional = sysRoleRepository.findOne(QSysRole.sysRole.isSystem.isTrue().and(QSysRole.sysRole.id.eq(id)));
        if (roleOptional.isPresent()) {
            return roleOptional.get();
        }
        throw new RuntimeException("默认角色[" +  id + "] 在数据库不存在");
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
