package com.miya.system.module.role.model;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.extra.spring.SpringUtil;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.config.business.Business;
import com.miya.system.module.role.SysRoleService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.envers.Audited;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色表
 **/
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(indexes = {@Index(name = "name_unique", columnList = "name", unique = true)})
@Audited
public class SysRole extends BaseEntity {

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 10)
    private Integer sequence;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private Boolean isSystem;

    /**
     * 映射拥有的权限，该权限随role的删除而删除
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sys_permission", joinColumns = {@JoinColumn(name = "role_id", nullable = false)})
    @Column(name = "code", length = 100, nullable = false)
    private Set<String> permissions = new ConcurrentHashSet<>();

    public Set<Business> getBusiness(){
        SysRoleService bean = SpringUtil.getBean(SysRoleService.class);
        return this.permissions.stream().map(bean::valueOfCode).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysRole sysRole = (SysRole) o;

        return id != null && id.equals(sysRole.id);
    }

    @Override
    public int hashCode() {
        return 915389366;
    }

    @Override
    public String toString() {
        return name;
    }
}
