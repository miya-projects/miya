package com.miya.system.config.filter.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author 杨超辉
 * 特殊字符过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class SpecialCharacterFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(new StringFilterRequest((HttpServletRequest) request), response);
    }

}

class StringFilterRequest extends HttpServletRequestWrapper {

    /**
     * ZWSP 零宽空格（空格）
     * ZWNBSP 零宽文本占位符
     * 等其他的不可见字符
     */
    private final String[] UN_AVAILABLE_CHARACTERS = {"\u200B", "\uFEFF", "\u00A0", "\u202F", "\u2028", "\u2029", "\u200E"};


    public StringFilterRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        // 返回值之前 先进行过滤
        return filterDangerString(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        // 返回值之前 先进行过滤
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = filterDangerString(values[i]);
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>();
        Map<String, String[]> keys = super.getParameterMap();
        Set<String> set = keys.keySet();
        for (String key : set) {
            Object value = keys.get(key);
            map.put(key, filterDangerString((String[]) value));
        }
        return map;
    }

    /**
     * 过滤数组中的非法字符
     * @param value
     */
    public String[] filterDangerString(String[] value) {
        if (value == null) {
            return null;
        }
        for (int i = 0; i < value.length; i++) {
            String val = filterDangerString(value[i]);
            value[i] = val;
        }
        return value;
    }


    /**
     * 过滤字符串中的非法字符
     * @param value 待过滤字符串
     * @return 过滤后的字符串
     */
    public String filterDangerString(String value) {
        if (value == null) {
            return null;
        }
        for (String unAvailableCharacter : UN_AVAILABLE_CHARACTERS) {
            value = value.replace(unAvailableCharacter, "");
        }
        return value;
    }

}
