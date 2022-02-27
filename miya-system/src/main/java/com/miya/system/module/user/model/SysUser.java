package com.miya.system.module.user.model;

import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.base.ReadableEnum;
import com.miya.common.module.bod.BackupOnDelete;
import com.miya.system.module.department.SysDepartment;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.module.role.SysDefaultRoles;
import com.miya.system.module.role.model.SysRole;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.*;
import org.springframework.security.core.AuthenticatedPrincipal;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户PO类
 */
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@BackupOnDelete
@Accessors(chain = true)
@FilterDef(name = "orderOwnerFilter",
        parameters = {@ParamDef(name = "ownerIds", type = "string")})
@Filters({@Filter(name = "orderOwnerFilter", condition = "id in (:ownerIds)")})
@Table(indexes = {@Index(name = "avatar", columnList = "avatar_id")})
public class SysUser extends BaseEntity implements AuthenticatedPrincipal {

    @Column(unique = true, length = 20)
    private String username;

    @Column(length = 60)
    private String password;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('NORMAL', 'LOCKED')")
    private AccountStatus accountStatus = AccountStatus.NORMAL;

    @Column(length = 512)
    private String remark;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('MALE', 'FEMALE')")
    private Sex sex;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns = {@JoinColumn(name = "user_id")}
            , inverseJoinColumns = {@JoinColumn(name = "department_id")}, name = "sys_user_department")
    private Set<SysDepartment> departments;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(joinColumns = {@JoinColumn(name = "user_id")}
            , inverseJoinColumns = {@JoinColumn(name = "role_id")}, name = "sys_user_role")
    private Set<SysRole> roles;

    @ManyToOne
    @JoinColumn(name = "avatar_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private SysFile avatar;

    /**
     * 偏好配置
     */
    @Type(type = "json")
    @Column(name = "preferences", columnDefinition = "json")
    private Preferences preferences = new Preferences();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private Set<SysUserSocial> sysUserSocials = new HashSet<>();

    /**
     * 获取头像 头像为空时返回默认头像
     */
    public SysFile getAvatar() {
        return Optional.ofNullable(this.avatar).orElse(SysFileService.DEFAULT_AVATAR);
    }

    /**
     * @return 该用户有拥有的权限数据
     */
    public Set<String> getBusiness() {
        //如果用户为超级管理员，则拥有所有权限
        if (isSuperAdmin()) {
            return new HashSet<>(Collections.singletonList("*"));
        }
        return this.roles.stream().flatMap(sysRole -> sysRole.getPermissions().stream()).collect(Collectors.toSet());
    }

    @Getter
    @AllArgsConstructor
    public enum AccountStatus implements ReadableEnum {
        NORMAL("正常"),
        LOCKED("锁定");
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public enum Sex implements ReadableEnum {
        MALE("男"),
        FEMALE("女");
        private final String name;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class Preferences{
        // 前端使用
        String front;
    }

    /**
     * 是不是管理员
     */
    public boolean isAdmin(){
        return SysDefaultRoles.ADMIN.hasThisRole(this);
    }

    /**
     * 判断一个用户是不是超级管理员 该管理员不应运用在普通业务场景中，
     * 超级管理员的意义是在系统需要运维时，运维人员使用超级管理员登录进行运维管理，普通业务场景下的管理员使用管理员角色来控制
     * id为1的用户是超级管理员
     */
    public boolean isSuperAdmin(){
        return "1".equals(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysUser sysUser = (SysUser) o;

        return id != null && id.equals(sysUser.id);
    }

    @Override
    public int hashCode() {
        return 421708296;
    }
}

