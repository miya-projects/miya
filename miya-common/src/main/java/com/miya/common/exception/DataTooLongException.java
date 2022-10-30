package com.miya.common.exception;

import lombok.Getter;
import org.hibernate.JDBCException;
import java.sql.SQLException;

/**
 * 数据太长
 * 封装JDBC异常用于捕获处理，默认情况的异常没有这么精确，不能单独针对一种情况进行处理
 */
public class DataTooLongException extends JDBCException {

    @Getter
    private final String filedName;

    public DataTooLongException(String message, SQLException cause, String filedName) {
        super(message, cause);
        this.filedName = filedName;
    }
}
