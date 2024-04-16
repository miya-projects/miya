package com.miya.system.module.common.po;


import com.miya.common.module.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
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
@Table(indexes = {@Index(name = "key_expire_date", columnList = "`key`, expireDate")})
@Comment("kv数据存储")
public class SysCache extends BaseEntity {

    @Column(name = "`key`", length = 100, nullable = false)
    private String key;

    @Column(name = "val", length = 16_777_215)
    @JdbcTypeCode(Types.CLOB)
    private String val;

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
