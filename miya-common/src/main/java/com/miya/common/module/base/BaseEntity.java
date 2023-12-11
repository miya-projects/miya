package com.miya.common.module.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.miya.common.config.orm.annotations.NoDashesUuidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * PO基类
 * @author 杨超辉
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@FieldNameConstants
@Audited
public abstract class BaseEntity implements Serializable, Persistable<Serializable> {

    @Id
    @Column(name = "id", length = 32)
    @JdbcTypeCode(Types.CHAR)
    @NoDashesUuidGenerator
    protected String id;

    protected BaseEntity() {
    }

    /**
     * 创建时间戳 (单位:秒)
     */
    @Column(nullable = false, updatable = false)
    @CreatedDate
    protected LocalDateTime createdTime;

    /**
     * 更新时间戳 (单位:秒)
     */
    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime updatedTime;

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
