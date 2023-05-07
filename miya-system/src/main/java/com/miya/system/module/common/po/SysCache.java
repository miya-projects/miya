package com.miya.system.module.common.po;


import com.miya.common.module.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * 可持久化k-v存储
 */
@Getter
@Setter
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {@Index(name = "s_key_expire_date", columnList = "skey, expireDate")})
public class SysCache extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String sKey;

    @Column(columnDefinition = "text")
    private String sVal;

    private Date expireDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysCache sysCache = (SysCache) o;
        return Objects.equals(id, sysCache.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
