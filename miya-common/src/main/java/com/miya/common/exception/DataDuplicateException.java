package com.miya.common.exception;

import lombok.Getter;
import org.hibernate.JDBCException;
import java.sql.SQLException;

/**
 * 数据重复
 * 封装JDBC异常用于捕获处理，默认情况的异常没有这么精确，不能单独针对一种情况进行处理
 */
public class DataDuplicateException extends JDBCException {

    @Getter
    private final String filedName;

    @Getter
    private final String value;

    public DataDuplicateException(String message, SQLException cause, String filedName, String value) {
        super(message, cause);
        this.value = value;
        this.filedName = filedName;
    }
}
