package com.miya.system.config.filter.http;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 杨超辉
 * response对象包装类 在filter中实现response数据的统一处理
 */
public class MyHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter charArrayWriter = new CharArrayWriter();
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


    public MyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (null != byteArrayOutputStream && !StringUtils.isBlank(byteArrayOutputStream.toString())) {
            return new PrintWriter(byteArrayOutputStream);
        } else {
            return new PrintWriter(charArrayWriter);
        }
    }

    public CharArrayWriter getCharArrayWriter() {
        return charArrayWriter;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void clear() {
        charArrayWriter = new CharArrayWriter();
        byteArrayOutputStream = new ByteArrayOutputStream();
    }
}
