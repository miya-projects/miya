package com.miya.common.config.web.databind;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局输入参数处理
 */
@Slf4j
@ControllerAdvice
@RestControllerAdvice(annotations = RestController.class)
public class ControllerAdviceInitBinder {

    /**
     * 不可见字符
     */
    public static final String[] UN_AVAILABLE_CHARACTERS = {"\u200B", "\uFEFF", "\u00A0", "\u202F", "\u2028", "\u2029", "\u200E"};
    public static final String UN_AVAILABLE_STRING = String.join("", UN_AVAILABLE_CHARACTERS);


    @Value("${config.enable-special-character-filter}")
    private Boolean enableSpecialCharacterFilter;

    @InitBinder
    void processParam(WebDataBinder binder) {
        // 1. 删除不可见字符 2. 空字符串按NULL处理
        if (enableSpecialCharacterFilter){
            binder.registerCustomEditor(String.class, new StringTrimmerEditor(UN_AVAILABLE_STRING, true));
        }
    }

}
