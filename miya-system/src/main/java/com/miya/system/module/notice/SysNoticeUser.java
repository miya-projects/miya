package com.miya.system.module.notice;

import com.miya.system.module.user.model.SysUser;
import lombok.*;
import org.hibernate.annotations.NotFound;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sys_notice_user")
public class SysNoticeUser /* implements Persistable<SysNoticeUser.SysNoticeUserId>*/ {

    // @Override
    // public boolean isNew() {
    //     return this.id == null;
    // }

    @Embeddable
    @Getter
    @Setter
    @EqualsAndHashCode
    static class SysNoticeUserId implements Serializable {
        @ManyToOne
        @JoinColumn(name = "notice_id")
        private SysNotice sysNotice;

        @ManyToOne
        @JoinColumn(name = "user_id")
        @NotFound
        private SysUser sysUser;
    }

    /**
     * 创建时间戳 (单位:秒)
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    protected Date createdTime = new Date();

    /**
     * 更新时间戳 (单位:秒)
     */
    protected Date updatedTime;

    // @ManyToOne
    // @JoinColumn(name = "notice_id")
    // private SysNotice sysNotice;
    //
    // @ManyToOne
    // @JoinColumn(name = "user_id")
    // private SysUser sysUser;

    @EmbeddedId
    private SysNoticeUserId id;

    /**
     * 0未读1已读
     */
    @Builder.Default
    @Column(name = "`read`")
    private Boolean read = Boolean.FALSE;
}
