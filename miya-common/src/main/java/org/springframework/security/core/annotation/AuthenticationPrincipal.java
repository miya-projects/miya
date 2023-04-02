package org.springframework.security.core.annotation;

import java.lang.annotation.*;

/**
 * 获取当前登录用户
 * @see com.miya.common.config.web.AuthenticationPrincipalHandlerMethodArgumentResolver
 */
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticationPrincipal {
}
