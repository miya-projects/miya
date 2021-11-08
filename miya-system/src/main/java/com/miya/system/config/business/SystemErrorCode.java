package com.miya.system.config.business;

import com.miya.common.model.dto.base.ResponseCode;
import lombok.Getter;

/**
 * 系统错误码
 */
@Getter
public enum SystemErrorCode implements ResponseCode {

    CAN_NOT_OPERATE_SUPER_ADMIN("该用户为超级管理员,不可进行该操作"),
    OPE_SYSTEM_ROLE("不允许操作系统角色");

    private final int code;
    private final String msg;

    SystemErrorCode(String msg){
        this.msg = msg;
        this.code = 201 + this.ordinal();
    }

}
