package com.miya.common.config.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.miya.common.annotation.RequestJsonParam;
import com.miya.common.util.JSONUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * @author 杨超辉
 * 实现json传参方式中部分参数接收的参数解析器 在json 参数中使用@RequestJson("参数名")即可
 * 没配置参数名默认为变量名
 * todo openapi3支持
 */
@RequiredArgsConstructor
public class RequestJsonHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 第一次读流后存到request对象的name，存储是为了多个参数下一次使用，流只能读一次
     */
    private static final String REQUEST_ATTR_NAME = "com.miya.common.config.web.RequestJsonHandlerMethodArgumentResolver.requestAttrName";

    private final ConversionService conversionService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestJsonParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HashMap<String, Object> map = JSONUtils.toJavaObject(getRequestJsonData(webRequest),new TypeReference<>() {});
        if (Objects.isNull(map)) {
            return null;
        }
        RequestJsonParam requestJsonParam = AnnotationUtils.findAnnotation(parameter.getParameter(), RequestJsonParam.class);
        assert requestJsonParam != null;
        String value = requestJsonParam.value();
        //如果没有指定value就使用参数变量名
        if (StringUtils.isEmpty(value)) {
            value = parameter.getParameterName();
        }
        return conversionService.convert(map.get(value), parameter.getParameterType());
    }

    /**
     * 获取请求的json数据
     *
     * @param request
     * @return
     * @throws IOException
     */
    private String getRequestJsonData(NativeWebRequest request) throws IOException {
        Object attribute = request.getAttribute(REQUEST_ATTR_NAME, NativeWebRequest.SCOPE_REQUEST);
        if (Objects.nonNull(attribute)) {
            return attribute.toString();
        }
        HttpServletRequest httpServletRequest = request.getNativeRequest(HttpServletRequest.class);
        BufferedReader reader = httpServletRequest.getReader();
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int rd;
        while ((rd = reader.read(buf)) != -1) {
            sb.append(buf, 0, rd);
        }
        request.setAttribute(REQUEST_ATTR_NAME, sb.toString(), NativeWebRequest.SCOPE_REQUEST);
        return sb.toString();
    }

}
