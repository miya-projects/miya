package com.teamytd.config.web;

import com.miya.common.model.dto.base.R;
import com.miya.system.module.email.EMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 用于处理常见的通用异常
 * {@link com.miya.common.config.web.GlobalExceptionHandler}
 */
@Slf4j
@Order(100)
@RestControllerAdvice
@RequiredArgsConstructor
public class UnknownExceptionNotice {

    private final EMailService eMailService;

    /**
     * 系统未知异常处理
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> commonException(Exception e) {
        log.error(ExceptionUtils.getStackTrace(e));
        eMailService.sendLog(ExceptionUtils.getStackTrace(e));
        return R.errorWithMsg("系统未知异常，请稍后重试");
    }
}
