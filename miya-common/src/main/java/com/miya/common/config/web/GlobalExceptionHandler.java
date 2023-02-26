package com.miya.common.config.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.miya.common.exception.DataTooLongException;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import com.miya.common.util.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用于处理常见的通用异常
 * 如果需要覆盖该处理，将@Order顺序调整为小于10即可
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常回调
     */
    @ExceptionHandler({BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> bindException(HttpServletResponse response, BindException e) {
        //没有对应的converter转换参数，打印异常信息处理
        // log.error(ExceptionUtils.getStackTrace(e));
        List<ObjectError> allErrors = e.getAllErrors();
        String message = allErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(","));
        response.setContentType(ContentType.JSON.toString());
        return R.errorWithMsg(StrUtil.format("参数校验失败: {}", message));
    }

    /**
     * 不合法的a.b格式的传参
     */
    @ExceptionHandler({NullValueInNestedPathException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> nullValueInNestedPathException(NullValueInNestedPathException e) {
        return R.errorWithMsg(StrUtil.format("参数校验失败: 参数[{}]格式错误", e.getPropertyName()));
    }

    /**
     * 参数校验异常回调
     * @param e
     * @param response
     */
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> constraintViolationException(HttpServletResponse response, ConstraintViolationException e) {
        StringBuilder errorMessage = new StringBuilder("参数校验失败:");
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        String message = constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(","));
        errorMessage.append(message);
        response.setContentType(ContentType.JSON.toString());
        return R.errorWithMsg(errorMessage.toString());
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<?> httpMediaTypeNotSupportedException(HttpServletResponse response, HttpMediaTypeNotSupportedException e) {
        List<MediaType> supportedMediaTypes = e.getSupportedMediaTypes();
        String mediaTypes = supportedMediaTypes.stream().map(MimeType::toString).collect(Collectors.joining(","));
        return R.errorWithMsg("媒体类型不支持，支持的媒体类型有:" + mediaTypes);
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<?> httpMediaTypeNotSupportedException(HttpServletResponse response, HttpRequestMethodNotSupportedException e) {
        return R.errorWithMsg("http method不允许");
    }


    @ExceptionHandler({ErrorMsgException.class})
    @ResponseStatus(HttpStatus.OK)
    public R<?> responseMsgException(HttpServletResponse response, ErrorMsgException e) {
        return R.errorWithMsg(e.getMessage());
    }

    @ExceptionHandler({ResponseCodeException.class})
    @ResponseStatus(HttpStatus.OK)
    public R<?> responseMsgException(HttpServletResponse response, ResponseCodeException e) {
        return R.errorWithCodeAndMsg(e.getResponseCode(), e.getArgs());
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> maxUploadSizeExceededException(HttpServletResponse response, MaxUploadSizeExceededException e) {
        return R.errorWithMsg("上传文件大小超过最大值");
    }

    @ExceptionHandler({DataTooLongException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> dataTooLongException(HttpServletResponse response, DataTooLongException e) {
        return R.errorWithMsg(StrUtil.format("{}输入太长", e.getFiledName()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> missingServletRequestParameterException(HttpServletResponse response, MissingServletRequestParameterException e) {
        return R.errorWithMsg(StrUtil.format("参数{}是必须的", e.getParameterName()));
    }

    @ExceptionHandler({MissingPathVariableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> missingPathVariableException(HttpServletResponse response, MissingPathVariableException e) {
        return R.errorWithMsg(StrUtil.format("参数{}是必须的", e.getVariableName()));
    }

    @ExceptionHandler({MultipartException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> multipartException(HttpServletResponse response, MultipartException e) {
        return R.errorWithMsg(StrUtil.format("{}", e.getMessage()));
    }

    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public void httpMediaTypeNotAcceptableException(HttpServletResponse response, HttpMediaTypeNotAcceptableException e) throws IOException {
        R<Object> r = R.errorWithMsg(StrUtil.format("{}", e.getMessage()));
        response.getWriter().write(Objects.requireNonNull(JSONUtils.toJson(r)));
        response.setContentType(ContentType.JSON.toString());
    }

    /**
     * 忽略客户端中断异常
     * @param response
     * @param e
     */
    @ExceptionHandler({ClientAbortException.class})
    public void httpMediaTypeNotAcceptableException(HttpServletResponse response, ClientAbortException e) {}


}
