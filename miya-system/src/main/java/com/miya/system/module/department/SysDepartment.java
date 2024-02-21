package com.miya.system.module.department;

import com.miya.common.module.base.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Comments;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
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
@Table(indexes = {@Index(name = "name_unique", columnList = "pid, name", unique = true)})
@Audited
@Comment("部门")
public class SysDepartment extends BaseEntity {

    @Column(length = 100, nullable = false)
    @Comment("部门名称")
    private String name;

    @Column(length = 200)
    @Comment("部门描述")
    private String description;

    @ManyToOne
    @JoinColumn(name = "pid")
    @Comment("上级部门")
    private SysDepartment parent;

    @OneToMany(orphanRemoval = true, mappedBy = "parent")
    private List<SysDepartment> children;

    @Type(JsonType.class)
    @Column(name = "extra", columnDefinition = "json")
    @Comment("额外信息")
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
