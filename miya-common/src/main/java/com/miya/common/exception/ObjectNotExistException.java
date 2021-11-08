package com.miya.common.exception;


/**
 * 对象不存在异常
 */
public class ObjectNotExistException extends RuntimeException {

    public ObjectNotExistException(String msg) {
        super(msg);
    }

}
