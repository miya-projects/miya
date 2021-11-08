package com.miya.common.exception;

/**
 * 用于service层直接返回给前端信息
 */
public class ErrorMsgException extends RuntimeException{

    public ErrorMsgException(String msg){
        super(msg);
    }

    public ErrorMsgException(String message, Throwable cause) {
        super(message, cause);
    }
}
