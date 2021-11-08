package com.miya.common.exception;

/**
 * 参数为空异常
 */
public class ParamsIsNullException extends RuntimeException{

    public ParamsIsNullException(String msg){
        super(msg);
    }

}
