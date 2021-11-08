package com.miya.system.module.user.model;

import com.miya.common.service.mapper.DTOMapper;
import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.department.dto.SysDepartmentSimpleDTO;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.role.model.SysRoleSimpleDTO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@ApiModel
@Getter
@Setter
public class SysUserListDTO extends BaseDTO {

    private String username;

    private String name;

    private String remark;

    private String phone;

    private SysFile avatar;

    private SysUser.Sex sex;

    private Set<SysRoleSimpleDTO> roles;

    private SysUser.AccountStatus accountStatus;

    private Set<SysDepartmentSimpleDTO> departments;

    private boolean isSuperAdmin;

    public static SysUserListDTO of(SysUser user) {
        return Mapper.INSTANCE.toDto(user);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysUserListDTO, SysUser> {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);
    }

}
