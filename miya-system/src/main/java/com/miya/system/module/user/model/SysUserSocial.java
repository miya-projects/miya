package com.miya.system.module.user.model;

import com.miya.common.module.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * user_social
 * 用户社交平台关系
 */
@Entity
@Table(name = "sys_user_social", indexes = {@Index(name = "channel_social_id", columnList = "channel, social_id")})
@Getter
@Setter
@Accessors(chain = true)
public class SysUserSocial extends BaseEntity implements Serializable {


    /**
     * 渠道
     */
    @Column(name = "channel", length = 20)
    @EqualsAndHashCode.Include
    private String channel;

    @Column(name = "social_id", length = 32)
    @EqualsAndHashCode.Include
    private String socialId;

    /**
     * 绑定时的其他信息
     */
    @Column(name = "extra", columnDefinition = "json")
    @Type(type = "json")
    private Map<String, Object> extra;

    public static SysUserSocial ofDingUnion(String socialId) {
        return of(Channel.DING_UNION.name(), socialId);
    }

    public static SysUserSocial ofDingTalk(String socialId) {
        return of(Channel.DING_TALK.name(), socialId);
    }

    public static SysUserSocial of(String channel, String socialId) {
        SysUserSocial sysUserSocial = new SysUserSocial();
        sysUserSocial.setSocialId(socialId);
        sysUserSocial.setChannel(channel);
        return sysUserSocial;
    }

    public enum Channel {
        // 钉钉h5微应用
        DING_TALK,
        // 钉钉扫码登录
        DING_SCAN,
        // union id
        DING_UNION;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SysUserSocial that = (SysUserSocial) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
