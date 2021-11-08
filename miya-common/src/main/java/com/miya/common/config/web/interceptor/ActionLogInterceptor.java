package com.miya.common.config.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miya.common.model.dto.base.R;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author 杨超辉
 * api访问日志拦截器
 */
// @ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ActionLogInterceptor implements HandlerInterceptor, ResponseBodyAdvice<Object> {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private final ThreadLocal<String> url = new ThreadLocal<>();
    /**
     * 是否跳过当前请求
     */
    private final ThreadLocal<Boolean> skip = ThreadLocal.withInitial(() -> false);

    private final ObjectMapper objectMapper;

    /**
     * 是否打印响应内容
     */
    private final boolean printResponse;

    private final String[] excludes;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o) {
        startTime.set(System.currentTimeMillis());
        skip.set(false);
        if (!(o instanceof HandlerMethod)){
            return true;
        }

        String name = ((HandlerMethod) o).getBeanType().getName();
        for (String str : excludes) {
            if (name.startsWith(str)) {
                skip.set(true);
                break;
            }
        }

        if (this.skip.get()) {
            return true;
        }

        url.set(request.getRequestURI());
        StringBuilder parameterStr = new StringBuilder();
        Set<Map.Entry<String, String[]>> entries = request.getParameterMap().entrySet();
        entries.forEach( entry -> {
            String key = entry.getKey();
            String[] value = entry.getValue();
            parameterStr.append(StrUtil.format("\t{}: {}\n", key, Arrays.toString(value)));
        } );
        log.debug("\n接收到请求：\nurl：{} \nparameter: \n{}", url.get(), parameterStr);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {
        long useTime = System.currentTimeMillis() - startTime.get();
        if( useTime > 300 ){
            log.warn("接口耗时超过300毫秒报警：" + url.get() + " spent " + (System.currentTimeMillis() - startTime.get()) + "ms");
        }
        startTime.remove();
        url.remove();
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return R.class.isAssignableFrom(Objects.requireNonNull(returnType.getMethod()).getReturnType());
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!this.printResponse){
            return body;
        }
        if (this.skip.get()) {
            return body;
        }
        //有些4xx请求不会进入preHandle，也不需要log
        if (Objects.nonNull(url.get())){
            log.debug("\n接口{}返回:\n{}", url.get(), objectMapper.writeValueAsString(body));
        }
        return body;
    }
}
