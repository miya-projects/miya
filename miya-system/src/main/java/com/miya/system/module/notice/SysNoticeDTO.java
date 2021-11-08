package com.miya.system.module.notice;

import com.miya.common.module.base.BaseEntity;
import com.miya.common.service.mapper.DTOMapper;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

import java.util.Map;

@Getter
@Setter
@ApiModel
public class SysNoticeDTO extends BaseEntity {

    /**
     * 通知标题
     */
    private String title;
    /**
     * 通知内容
     */
    private String content;
    /**
     * 额外参数
     */
    private Map<String, Object> extra;

    /**
     * 是否启用
     */
    private Boolean enable;

    // /**
    //  * 是否已读
    //  */
    // private Boolean read;

    /**
     * 接收用户
     */
    // @Getter(AccessLevel.NONE)
    // private SysUserDTO sysUserDTO;

    public void assign(SysNoticeUser noticeUser){
        // this.read = noticeUser.getRead();
    }

    public static SysNoticeDTO of(SysNotice sysDict) {
        return SysNoticeDTO.Mapper.INSTANCE.toDto(sysDict);
    }

    @org.mapstruct.Mapper
    public interface Mapper extends DTOMapper<SysNoticeDTO, SysNotice> {
        SysNoticeDTO.Mapper INSTANCE = Mappers.getMapper(SysNoticeDTO.Mapper.class);
    }

}
