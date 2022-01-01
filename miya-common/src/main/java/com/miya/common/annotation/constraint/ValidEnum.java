package com.miya.common.annotation.constraint;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
import java.util.Arrays;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.validation.constraintvalidation.ValidationTarget.PARAMETERS;

/**
 * 将验证字段是否为仅允许的几个字段
 */
@Documented
@Constraint(validatedBy = {ValidEnum.FieldValidatorString.class, ValidEnum.FieldValidatorInteger.class})
@SupportedValidationTarget({ValidationTarget.ANNOTATED_ELEMENT, PARAMETERS})
@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Repeatable(ValidEnum.List.class)
public @interface ValidEnum {

    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的值
     */
    String[] allowValues() default {};

    /**
     * 校验器 校验String类型
     */
    @Slf4j
    class FieldValidatorString implements ConstraintValidator<ValidEnum, String> {

        private String message;
        private String[] allowValues;

        @Override
        public void initialize(ValidEnum constraintAnnotation) {
            this.allowValues = constraintAnnotation.allowValues();
            this.message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if(StringUtils.isBlank(this.message)){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{propertyName}不允许该值${validatedValue}，允许的值有: " + Arrays.toString(allowValues))
                        .addConstraintViolation();
            }
            java.util.List<String> allowValueList = Arrays.asList(allowValues);
            return allowValueList.contains(String.valueOf(value));
        }
    }

    /**
     * 校验器 校验Integer类型
     */
    @Slf4j
    class FieldValidatorInteger implements ConstraintValidator<ValidEnum, Integer> {

        private String message;
        private String[] allowValues;

        @Override
        public void initialize(ValidEnum constraintAnnotation) {
            this.allowValues = constraintAnnotation.allowValues();
            this.message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            if(StringUtils.isBlank(this.message)){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{propertyName}不允许该值${validatedValue}，允许的值有: " + Arrays.toString(allowValues))
                        .addConstraintViolation();
            }
            java.util.List<String> allowValueList = Arrays.asList(allowValues);
            return allowValueList.contains(String.valueOf(value));
        }
    }


    @Target({ FIELD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        ValidEnum[] value();
    }
}
