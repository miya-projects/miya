package com.miya.system.module.user.model;

import com.miya.common.module.base.BaseEntity;
import com.miya.system.config.web.ReadableEnum;
import com.miya.system.module.department.SysDepartment;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.oss.service.SysFileService;
import com.miya.system.module.role.SysDefaultRoles;
import com.miya.system.module.role.model.SysRole;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户PO类
 */
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Accessors(chain = true)
@FilterDef(name = "orderOwnerFilter",
        parameters = {@ParamDef(name = "ownerIds", type = String.class)})
@Filters({@Filter(name = "orderOwnerFilter", condition = "id in (:ownerIds)")})
@Table(indexes = {@Index(name = "avatar", columnList = "avatar_id"), @Index(name = "username", columnList = "username", unique = true)})
@Audited
@Comment("系统后台用户")
public class SysUser extends BaseEntity {

    /**
     * 登录用户名
     */
    @Column(length = 20, nullable = false)
    @Comment("登录用户名")
    private String username;

    /**
     * 用户登录密码
     */
    @Column(length = 60)
    @Comment("用户登录密码")
    private String password;

    /**
     * 用户姓名
     */
    @Column(length = 50)
    @Comment("用户姓名")
    private String name;

    /**
     * 账户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("账户状态")
    private AccountStatus accountStatus = AccountStatus.NORMAL;

    /**
     * 备注
     */
    @Column(length = 512)
    @Comment("备注")
    private String remark;

    /**
     * 手机号
     */
    @Column(length = 20)
    @Comment("手机号")
    private String phone;

    /**
     * 性别
     */
    @Comment("性别")
    @Enumerated(EnumType.STRING)
    private Sex sex;

    /**
     * 用户部门
     */
    @Comment("用户部门")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(joinColumns = {@JoinColumn(name = "user_id")}
            , inverseJoinColumns = {@JoinColumn(name = "department_id")}, name = "sys_user_department")
    private Set<SysDepartment> departments;

    /**
     * 用户角色
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(joinColumns = {@JoinColumn(name = "user_id")}
            , inverseJoinColumns = {@JoinColumn(name = "role_id")}, name = "sys_user_role")
    private Set<SysRole> roles;

    /**
     * 用户头像
     */
    @Comment("用户头像")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private SysFile avatar;

    /**
     * 偏好配置
     */
    @Comment("偏好配置")
    @Type(JsonType.class)
    @Column(name = "preferences", columnDefinition = "json")
    private Preferences preferences = new Preferences();

    /**
     * 用户社交账号
     */
    @Comment("用户社交账号")
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
        /**
         * 男
         */
        MALE("男"),
        /**
         * 女
         */
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

