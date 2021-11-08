package com.miya.common.module.config;

import com.miya.common.module.base.BaseEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Objects;

/**
 *  系统配置
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Accessors(chain = true)
public class SysConfig extends BaseEntity {

    public static final String GROUP_SYSTEM = "SYSTEM";

    /**
     * 变量key
     */
    @Column(name = "`key`")
    private String key;

    /**
     * 变量值
     */
    @Column(name = "val")
    private String val;

    /**
     * 配置项说明
     */
    @Column(name = "`desc`")
    private String desc;

    /**
     * 分组
     */
    @Column(name = "`group`")
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
