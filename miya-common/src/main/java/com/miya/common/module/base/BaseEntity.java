package com.miya.common.module.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miya.common.module.bod.BackupDataListener;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.Hibernate;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * PO基类
 * @author 杨超辉
 */
@TypeDefs(
        {
            @TypeDef(name = "json", typeClass = JsonType.class)
        }
)
@Getter
@Setter
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, BackupDataListener.class})
@FieldNameConstants
public abstract class BaseEntity implements Serializable, Persistable<Serializable> {

    @Id
    @Column(name = "id", columnDefinition = "char(32)")
    @GenericGenerator(name = "idGenerator", strategy = "com.miya.common.config.orm.ManualInsertGenerator")
    // @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    protected String id;

    protected BaseEntity() {
    }

    /**
     * 创建时间戳 (单位:秒)
     */
    @Column(nullable = false, updatable = false)
    @CreatedDate
    protected Date createdTime;

    /**
     * 更新时间戳 (单位:秒)
     */
    @LastModifiedDate
    @Column(nullable = false, updatable = true)
    protected Date updatedTime;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "created_user", length = 50)
    protected String createdUser;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 699169739;
    }
}
