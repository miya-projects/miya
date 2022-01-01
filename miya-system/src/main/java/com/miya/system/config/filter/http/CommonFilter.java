package com.miya.system.config.filter.http;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author 杨超辉
 */
//@Component
//@Configuration
@Slf4j
//@WebFilter(urlPatterns = "/graphql/**")
public class CommonFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("commonFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("doFilter Common");
        //重写response 的wrapper 缓存，阻止敏感词发送到页面
        MyHttpServletResponseWrapper myresp = new MyHttpServletResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, myresp);
        //得到myResponse输出的内容
        String output = myresp.getCharArrayWriter().toString();
        String outputs = myresp.getByteArrayOutputStream().toString();
        //response 没有提交 时进行提交
        if (!response.isCommitted()) {
            PrintWriter out = response.getWriter();
            out.write(StringUtils.isBlank(output) ? outputs : output);
            out.close();
        }
    }

    @Override
    public void destroy() {

    }
}
