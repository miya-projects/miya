package com.miya.system.module.department;

import com.miya.common.module.base.BaseEntity;
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
@Entity
@Accessors(chain = true)
public class SysDepartment extends BaseEntity {
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "pid")
    private SysDepartment parent;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "pid")
    private List<SysDepartment> children;

    @Type(type = "json")
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

}
