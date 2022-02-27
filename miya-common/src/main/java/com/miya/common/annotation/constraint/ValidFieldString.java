package com.miya.common.annotation.constraint;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronException;
import cn.hutool.cron.pattern.CronPattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.validation.constraintvalidation.ValidationTarget.PARAMETERS;

/**
 * String类型 静态字段校验 包含丰富的常见数据校验
 */
@Documented
@Constraint(validatedBy = {ValidFieldString.FieldValidator.class})
@SupportedValidationTarget({ValidationTarget.ANNOTATED_ELEMENT, PARAMETERS})
@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Repeatable(ValidFieldString.List.class)
public @interface ValidFieldString {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 校验类型
     */
    ValidType type() default ValidType.NONE;

    /**
     * 允许为空
     */
    boolean allowEmpty() default false;

    @Getter
    @AllArgsConstructor
    enum ValidType {
        NONE("", t -> true),
        PHONE("'${validatedValue}'不是一个手机号", Validator::isMobile),
        ID_CARD("'${validatedValue}'不是一个身份证号", Validator::isCitizenId),
        EMAIL("'${validatedValue}'不是一个电子邮件", Validator::isEmail),
        NUMBER("'${validatedValue}'不是一个数字", Validator::isNumber),
        WORD("'${validatedValue}'包含有非字母", Validator::isWord),
        MONEY("'${validatedValue}'不是货币数据", Validator::isMoney),
        ZIP_CODE("'${validatedValue}'不是邮政编码", Validator::isZipCode),
        PLATE_NUMBER("'${validatedValue}'不是中国有效车牌号", Validator::isPlateNumber),
        URL("'${validatedValue}'不是有效的url", Validator::isUrl),
        CHINESE("'${validatedValue}'包含有非汉字", Validator::isChinese),
        GENERAL("'${validatedValue}'不是标准的标识符", Validator::isGeneral),
        CRON_EXPRESS("'${validatedValue}'不是标准的cronTab表达式", (value -> {
            try {
                new CronPattern(value);
                return true;
            } catch (CronException e) {
                return false;
            }
        })),
        UUID("'${validatedValue}'不是UUID", val -> val.length() == 32);

        /**
         * 校验失败后的提示信息模版 可使用变量如下
         * the attribute values of the constraint mapped to the attribute names
         * <p>
         * the currently validated value (property, bean, method parameter etc.) under the name validatedValue
         * <p>
         * a bean mapped to the name formatter exposing the var-arg method format(String format, Object…​ args) which behaves like java.util.Formatter.format(String format, Object…​ args).
         */
        private final String message;
        private final FieldValidator fieldValidator;

        @FunctionalInterface
        interface FieldValidator {
            boolean checkField(String value);
        }
    }

    @Slf4j
    class FieldValidator implements ConstraintValidator<ValidFieldString, String> {

        private ValidType validType;
        private String message;
        private boolean allowEmpty;

        @Override
        public void initialize(ValidFieldString constraintAnnotation) {
            this.validType = constraintAnnotation.type();
            this.message = constraintAnnotation.message();
            this.allowEmpty = constraintAnnotation.allowEmpty();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            HibernateConstraintValidatorContext hibernateContext = context.unwrap(
                    HibernateConstraintValidatorContext.class );
            hibernateContext.disableDefaultConstraintViolation();
            hibernateContext
                    .addExpressionVariable( "validatedValue", value )
                    .addExpressionVariable( "property", "1111" )
                    // 这里的模版必须定死，不可用户动态输入，防止出现安全问题
                    .buildConstraintViolationWithTemplate(StrUtil.isBlank(this.message)?this.validType.message:this.message)
                    .enableExpressionLanguage(ExpressionLanguageFeatureLevel.BEAN_PROPERTIES)
                    .addConstraintViolation();

            if (this.allowEmpty && StringUtils.isEmpty(value)) {
                return true;
            }
            return this.validType.getFieldValidator().checkField(value);
        }
    }

    @Target({FIELD, PARAMETER, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        ValidFieldString[] value();
    }

}
