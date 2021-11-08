package com.miya.common.exception;

/**
 * 由于某种原因不能够删除资源而发生异常
 */
public class CannotDeleteException extends Exception {

    public CannotDeleteException(String message) {
        super(message);
    }

}
