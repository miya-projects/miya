package com.miya.system.module.notice;

import com.miya.common.module.base.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

import java.sql.Types;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class SysNotice extends BaseEntity {
    /**
     * 通知标题
     */
    @Column(length = 512)
    private String title;

    /**
     * 通知内容
     */
    @Column
    @JdbcTypeCode(Types.CLOB)
    private String content;

    /**
     * 额外参数
     * todo 具化类型?
     */
    @Type(JsonType.class)
    @Column(name = "extra", columnDefinition = "json")
    private Map<String, Object> extra;

    /**
     * 通知人
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "notice_id")
    private List<SysNoticeUser> sysUser;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enable = Boolean.TRUE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysNotice sysNotice = (SysNotice) o;

        return id != null && id.equals(sysNotice.id);
    }

    @Override
    public int hashCode() {
        return 1726629060;
    }
}
