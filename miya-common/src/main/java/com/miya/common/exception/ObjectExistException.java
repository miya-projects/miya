package com.miya.common.exception;


/**
 * 对象已存在异常
 */
public class ObjectExistException extends RuntimeException {

    public ObjectExistException(String msg) {
        super(msg);
    }

}
