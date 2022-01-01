package com.miya.common.config.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.exception.ResponseCodeException;
import com.miya.common.model.dto.base.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 用于处理常见的通用异常
 * 如果需要覆盖该处理，将@Order顺序调整为小于10即可
 */
@Slf4j
@Order(10)
@RestControllerAdvice
public class GlobalExceptionHandler {


    private final Pattern pattern = Pattern.compile("\\{propertyName}");

    /**
     * 参数校验异常回调
     */
    @ExceptionHandler({BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> bindException(HttpServletResponse response, BindException e) {
        StringBuilder errorMessage = new StringBuilder("参数校验失败:");
        BindingResult bindingResult = e.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            Optional<String> optional = Optional.ofNullable(fieldError.getDefaultMessage());
            errorMessage.append(pattern.matcher(optional.orElse("")).replaceAll(fieldError.getField()))
                    .append(", ");
        }
        response.setContentType(ContentType.JSON.toString());
        return R.errorWithMsg(errorMessage.toString());
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
        constraintViolations.forEach( constraintViolation -> {
            Path propertyPath = constraintViolation.getPropertyPath();
            List<Path.Node> nodes = StreamSupport.stream(propertyPath.spliterator(), false).collect(Collectors.toList());
            Path.Node property = nodes.get(nodes.size() - 1);
            errorMessage.append(constraintViolation.getMessage()
                    .replaceAll("\\{propertyName}", property.getName()))
                    .append(", ");
        } );
        response.setContentType("application/json");
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

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> missingServletRequestParameterException(HttpServletResponse response, MissingServletRequestParameterException e) {
        return R.errorWithMsg(StrUtil.format("参数 {}，是必须的", e.getParameterName()));
    }

    @ExceptionHandler({MissingPathVariableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> missingPathVariableException(HttpServletResponse response, MissingPathVariableException e) {
        return R.errorWithMsg(StrUtil.format("参数 {}，是必须的", e.getVariableName()));
    }

}
