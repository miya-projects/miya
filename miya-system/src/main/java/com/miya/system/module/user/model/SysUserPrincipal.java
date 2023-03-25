package com.miya.system.module.user.model;

import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.department.dto.SysDepartmentSimpleDTO;
import com.miya.system.module.oss.model.SysFileDTO;
import com.miya.system.module.role.model.SysRoleSimpleDTO;
import com.miya.system.module.user.SysUserRepository;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import java.util.Set;

/**
 * 用于spring security principal
 */
@Getter
@Setter
public class SysUserPrincipal extends BaseEntity {


    protected static ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.getConfiguration().setFullTypeMatchingRequired(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setPreferNestedProperties(false);
    }

    private String username;

    private String name;

    private String remark;

    private String phone;

    private SysFileDTO avatar;

    private SysUser.Sex sex;

    private Set<SysRoleSimpleDTO> roles;

    private SysUser.AccountStatus accountStatus;

    private Set<SysDepartmentSimpleDTO> departments;

    private boolean isSuperAdmin;

    private boolean isAdmin;

    private Set<String> business;

    private SysUser.Preferences preferences;

    public static SysUserPrincipal of(SysUser user) {
        return modelMapper.map(user, SysUserPrincipal.class);
    }

    public static Class<?> userType() {
        return SysUser.class;
    }

    public SysUser toPO() {
        SysUserRepository repository = SpringUtil.getBean(SysUserRepository.class);
        return repository.getReferenceById(this.id);
    }

}
