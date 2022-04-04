package com.miya.system.config.filter.interceptors;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

/**
 * 过滤UTF8编码中的不可见字符
 */
public class EscapeSensitiveWordFilter implements HandlerInterceptor {

    /**
     * ZWSP 零宽空格（空格）
     * ZWNBSP 零宽文本占位符
     * 等其他的不可见字符
     */
    private final String[] UN_AVAILABLE_CHARACTERS = {"\u200B", "\uFEFF", "\u00A0", "\u202F", "\u2028", "\u2029", "\u200E" };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o) {
        // 过滤请求参数中的不可见字符
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<String> keySet = parameterMap.keySet();
        for (String key : keySet) {
            String[] values = parameterMap.get(key);
            if (values != null && values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (StrUtil.isNotBlank(value)) {
                        for (String unAvailableCharacter : UN_AVAILABLE_CHARACTERS) {
                            value = value.replace(unAvailableCharacter, "");
                        }
                        values[i] = value;
                    }
                }
            }
        }
        return true;
    }

}
