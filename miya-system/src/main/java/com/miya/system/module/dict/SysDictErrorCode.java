package com.miya.system.module.dict;

import com.miya.common.model.dto.base.ResponseCode;
import lombok.Getter;

@Getter
public enum SysDictErrorCode implements ResponseCode {

    DICT_IN_USE("该字典正在被使用，不可操作")
    ;

    private final int code;
    private final String msg;

    SysDictErrorCode(String msg){
        this.msg = msg;
        this.code = 301 + this.ordinal();
    }

}
