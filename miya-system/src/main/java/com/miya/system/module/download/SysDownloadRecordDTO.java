package com.miya.system.module.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.miya.common.service.mapper.DTOMapper;
import com.miya.system.module.oss.model.SysFile;
import com.miya.system.module.user.model.SysUser;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;
import java.util.Date;
import java.util.Optional;

@Getter
@Setter
public class SysDownloadRecordDTO {

    private Date createdTime;

    private SysDownloadRecord.Status status;

    /**
     * 下载内容
     */
    private String name;

    private Date downloadTime;

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
        return SysDownloadRecordDTO.Mapper.INSTANCE.toDto(downloadRecord);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysDownloadRecordDTO, SysDownloadRecord> {
        SysDownloadRecordDTO.Mapper INSTANCE = Mappers.getMapper(SysDownloadRecordDTO.Mapper.class);
    }


}
