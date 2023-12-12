package com.miya.common.module.config;

import com.miya.common.module.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;

import java.sql.Types;
import java.util.Objects;

/**
 *  系统配置
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(indexes = {@Index(name = "key_unique", columnList = "group,key", unique = true)})
@Accessors(chain = true)
@Audited
public class SysConfig extends BaseEntity {

    public static final String GROUP_SYSTEM = "SYSTEM";

    /**
     * 变量key
     */
    @Column(name = "`key`", length = 50, nullable = false)
    private String key;

    /**
     * 变量值
     */
    @Column(name = "val", length = Integer.MAX_VALUE)
    @JdbcTypeCode(Types.CLOB)
    private String val;

    /**
     * 配置项说明
     */
    @Column(name = "`desc`", length = 200)
    private String desc;

    /**
     * 分组
     */
    @Column(name = "`group`", length = 30)
    private String group = GROUP_SYSTEM;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysConfig sysConfig = (SysConfig) o;

        return Objects.equals(id, sysConfig.id);
    }

    @Override
    public int hashCode() {
        return 637842328;
    }
}
