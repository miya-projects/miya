package com.miya.system.module.notice;

import com.miya.common.module.base.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema
public class SysNoticeDTO extends BaseDTO {

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

    public static SysNoticeDTO of(SysNotice sysDict) {
        return modelMapper.map(sysDict, SysNoticeDTO.class);
    }

}
