package com.miya.system.module.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.config.web.ReadableEnum;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.user.model.SysUser;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import java.util.Date;

@Table(name = "sys_download_record", indexes = {@Index(name = "status", columnList = "status")})
@Entity
@Getter
@Setter
@Comment("文件下载记录")
public class SysDownloadRecord extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "enum('WAITING', 'PROCESSING', 'COMPLETED', 'DOWNLOAD', 'FAILED')")
    private Status status = Status.WAITING;

    /**
     * 下载内容
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "download_time")
    private Date downloadTime;

    @Column(name = "completed_time")
    private Date completedTime;

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

    @Type(JsonType.class)
    @Column(name = "extra", columnDefinition = "json")
    private JsonNode extra;

    @Getter
    @RequiredArgsConstructor
    public enum Status implements ReadableEnum {

        WAITING("等待中"),
        PROCESSING("处理中"),
        COMPLETED("处理完成"),
        DOWNLOAD("已下载"),
        FAILED("处理失败");
        private final String name;
    }

}
