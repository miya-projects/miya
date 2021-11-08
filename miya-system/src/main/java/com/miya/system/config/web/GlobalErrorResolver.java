package com.miya.system.config.web;

import cn.hutool.core.bean.BeanUtil;
import com.miya.common.model.dto.base.R;
import com.miya.common.model.dto.base.ResponseCode;
import com.miya.system.module.email.EMailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 杨超辉
 * @date 2018/7/27
 * @description 错误全局处理
 * todo 这里需要统筹规划 优化
 */
// @Component
// @RequestMapping("/error")
@Slf4j
@ApiIgnore
public class GlobalErrorResolver extends BasicErrorController implements HandlerExceptionResolver {
    @Resource
    private EMailService eMailService;

    public GlobalErrorResolver(ServerProperties serverProperties) {
        super(new DefaultErrorAttributes(), serverProperties.getError(), buildErrorViewResolver());
    }

    /**
     * 构建错误视图解析器
     *
     * @return
     */
    public static List<ErrorViewResolver> buildErrorViewResolver() {
        List<ErrorViewResolver> errorViewResolvers = new ArrayList<>();
        errorViewResolvers.add((request, status, model) -> status == HttpStatus.FORBIDDEN ? new ModelAndView("/error/403.html", model) : null);
        errorViewResolvers.add((request, status, model) -> status == HttpStatus.NOT_FOUND ? new ModelAndView("/error/404.html", model) : null);
        errorViewResolvers.add((request, status, model) -> status == HttpStatus.INTERNAL_SERVER_ERROR ? new ModelAndView("/error/500.html", model) : null);
        return errorViewResolvers;
    }

    /**
     * 覆盖默认的Json响应
     *
     * @return
     */
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        R<?> r;
        if (body.get("status").toString().equals("404")) {
            r = R.errorWithCodeAndMsg(ResponseCode.Common.PATH_NOT_EXIST, body.get("path").toString());
        } else {
            r = R.errorWithMsg(body.get("error").toString());
        }
        return new ResponseEntity<>(BeanUtil.beanToMap(r), HttpStatus.OK);
    }

    /**
     * 覆盖默认的HTML响应
     */
    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        //请求的状态
        HttpStatus status = getStatus(request);
        response.setStatus(getStatus(request).value());
        Map<String, Object> model = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        //404转发给index
        //        if (status == HttpStatus.NOT_FOUND) {
        //            return new ModelAndView(new MappingJackson2JsonView(), model);
        //        }
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        return modelAndView == null ? new ModelAndView("error/error.html", model) : modelAndView;
    }

    /**
     * 异常处理器
     *
     * @param request
     * @param response
     * @param handler
     * @param exception
     * @return
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception exception) {
        log.debug(ExceptionUtils.getStackTrace(exception));
        R<?> r = R.errorWithMsg(exception.toString());
        if (exception instanceof MaxUploadSizeExceededException) {
            r = R.errorWithCodeAndMsg(ResponseCode.Common.FILE_TOO_BIG);
        } else {
            eMailService.sendLog(ExceptionUtils.getStackTrace(exception));
        }
        return new ModelAndView(new MappingJackson2JsonView(), BeanUtil.beanToMap(r));
    }
}
