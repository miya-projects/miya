package com.miya.system.module.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.config.web.ReadableEnum;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.user.model.SysUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import javax.persistence.*;
import java.util.Date;

@Table(name = "sys_download_record")
@Entity
@Getter
@Setter
public class SysDownloadRecord extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "enum('PROCESSING', 'COMPLETED', 'DOWNLOAD')")
    private Status status = Status.PROCESSING;

    /**
     * 下载内容
     */
    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "download_time")
    private Date downloadTime;

    @Column(name = "file_name", length = 100)
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "file_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private SysFile file;

    /**
     * 任务下载人
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private SysUser user;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonNodeStringType")
    @Column(name = "extra", columnDefinition = "json")
    private JsonNode extra;

    @Getter
    @RequiredArgsConstructor
    public enum Status implements ReadableEnum {
        PROCESSING("处理中"),
        COMPLETED("处理完成，待下载"),
        DOWNLOAD("已下载");
        private final String name;
    }

}
