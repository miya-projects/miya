package com.miya.common.exception;

/**
 * 进行危险操作时发生此异常
 */
public class DangerousOperationException extends RuntimeException {

    public DangerousOperationException(String msg) {
        super(msg);
    }

}
