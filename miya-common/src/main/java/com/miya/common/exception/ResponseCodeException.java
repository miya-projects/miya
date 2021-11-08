package com.miya.common.exception;

import com.miya.common.model.dto.base.ResponseCode;
import lombok.Getter;

/**
 * 发生该异常时将返回ResponseCode中对应的消息
 */
@Getter
public class ResponseCodeException extends RuntimeException{

    private final ResponseCode responseCode;
    private final String[] args;

    public ResponseCodeException(ResponseCode responseCode, String... args){
        this.responseCode = responseCode;
        this.args = args;
    }

}
