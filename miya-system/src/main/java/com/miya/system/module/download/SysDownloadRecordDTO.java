package com.miya.system.module.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.miya.common.module.base.BaseDTO;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.user.model.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;

@Getter
@Setter
@Schema
public class SysDownloadRecordDTO extends BaseDTO {

    private Date createdTime;

    private SysDownloadRecord.Status status;

    /**
     * 下载内容
     */
    private String name;

    private Date downloadTime;

    private Date completedTime;

    private String fileName;

    private SysFile file;

    /**
     * 任务下载人
     */
    private String user;

    private JsonNode extra;

    public void setUser(SysUser user) {
        this.user = Optional.ofNullable(user).map(SysUser::getName).orElse("");
    }

    public static SysDownloadRecordDTO of(SysDownloadRecord downloadRecord) {
        return modelMapper.map(downloadRecord, SysDownloadRecordDTO.class);
    }

}
