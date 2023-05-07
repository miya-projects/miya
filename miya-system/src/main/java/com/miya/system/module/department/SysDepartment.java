package com.miya.system.module.department;

import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.bod.BackupOnDelete;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 部门
 */
@Getter
@Setter
@NoArgsConstructor
@BackupOnDelete
@Entity
@Accessors(chain = true)
@Table(indexes = {@Index(name = "name_unique", columnList = "pid, name", unique = true)})
public class SysDepartment extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 200)
    private String description;

    @ManyToOne
    @JoinColumn(name = "pid")
    private SysDepartment parent;

    @OneToMany(orphanRemoval = true, mappedBy = "parent")
    private List<SysDepartment> children;

    @Type(type = "json")
    @Column(name = "extra", columnDefinition = "json")
    private Map<String, Object> extra;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysDepartment that = (SysDepartment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
