package com.miya.common.annotation.constraint;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.miya.common.config.orm.MetadataExtractorIntegrator;
import com.miya.common.module.base.BaseForm;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.validation.constraintvalidation.ValidationTarget.PARAMETERS;

/**
 * String类型 字段长度校验，读取数据库字段长度作为最大长度要求
 */
@Documented
@Constraint(validatedBy = {AutoMaxLength.BaseFormFieldValidator.class/*, AutoValidLength.StringFieldValidator.class*/})
@SupportedValidationTarget({ValidationTarget.ANNOTATED_ELEMENT, PARAMETERS})
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
public @interface AutoMaxLength {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 是否开启校验
     */
    boolean enabled() default true;

    /**
     * 指定实体类，如果不指定则读取父类BaseForm泛型
     */
    Class<?> entityClass() default Void.class;


    // @Slf4j
    // class StringFieldValidator implements ConstraintValidator<AutoValidLength, String> {
    //
    //     private String message;
    //     private boolean enabled;
    //     private Class<?> entityClass;
    //     private static final String DEFAULT_MESSAGE = "{}: 您的输入太长了";
    //
    //     @Override
    //     public void initialize(AutoValidLength constraintAnnotation) {
    //         this.message = constraintAnnotation.message();
    //         this.enabled = constraintAnnotation.enabled();
    //         this.entityClass = constraintAnnotation.entityClass();
    //     }
    //
    //     @SneakyThrows
    //     @Override
    //     public boolean isValid(String value, ConstraintValidatorContext context) {
    //         HibernateConstraintValidatorContext hibernateContext = context.unwrap(
    //                 HibernateConstraintValidatorContext.class );
    //         hibernateContext.disableDefaultConstraintViolation();
    //
    //         if (!this.enabled){
    //             return true;
    //         }
    //         Class<?> poClass = this.entityClass;
    //         if (this.entityClass == Void.class){
    //             poClass = ClassUtil.getTypeArgument(value.getClass());
    //         }
    //         if (poClass == null){
    //             log.warn("未在类{}中读取到实体类，AutoValidLength注解将不生效", value.getClass());
    //             return true;
    //         }
    //
    //         BeanInfo beanInfo = Introspector.getBeanInfo(poClass);
    //         PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    //         java.util.List<MetadataExtractorIntegrator.PropertyLength> stringColumns = MetadataExtractorIntegrator.INSTANCE.getStringColumns(poClass);
    //         for (MetadataExtractorIntegrator.PropertyLength stringColumn : stringColumns) {
    //             for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
    //                 if (propertyDescriptor.getName().equals(stringColumn.name)) {
    //                     Object result = propertyDescriptor.getReadMethod().invoke(value);
    //                     if (result == null){
    //                         continue;
    //                     }
    //                     if (result.toString().length() > stringColumn.length) {
    //                         String msg = StrUtil.isBlank(this.message)?DEFAULT_MESSAGE:this.message;
    //                         hibernateContext.buildConstraintViolationWithTemplate(StrUtil.format(msg, stringColumn.name))
    //                                 .addConstraintViolation();
    //                         return false;
    //                     }
    //                 }
    //             }
    //
    //         }
    //         return true;
    //     }
    // }

    @Slf4j
    class BaseFormFieldValidator implements ConstraintValidator<AutoMaxLength, BaseForm<?>> {

        private String message;
        private boolean enabled;
        private Class<?> entityClass;
        private static final String DEFAULT_MESSAGE = "{}: 您的输入太长了";

        @Override
        public void initialize(AutoMaxLength constraintAnnotation) {
            this.message = constraintAnnotation.message();
            this.enabled = constraintAnnotation.enabled();
            this.entityClass = constraintAnnotation.entityClass();
        }

        @SneakyThrows
        @Override
        public boolean isValid(BaseForm value, ConstraintValidatorContext context) {
            HibernateConstraintValidatorContext hibernateContext = context.unwrap(
                    HibernateConstraintValidatorContext.class );
            hibernateContext.disableDefaultConstraintViolation();

            if (!this.enabled){
                return true;
            }
            Class<?> poClass = this.entityClass;
            if (this.entityClass == Void.class){
                poClass = ClassUtil.getTypeArgument(value.getClass());
            }
            if (poClass == null){
                log.warn("未在类{}中读取到实体类，AutoValidLength注解将不生效", value.getClass());
                return true;
            }

            BeanInfo beanInfo = Introspector.getBeanInfo(value.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            java.util.List<MetadataExtractorIntegrator.PropertyLength> stringColumns = MetadataExtractorIntegrator.INSTANCE.getStringColumns(poClass);
            for (MetadataExtractorIntegrator.PropertyLength stringColumn : stringColumns) {
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor.getName().equals(stringColumn.name)) {
                        Object result = ReflectUtil.invoke(value, propertyDescriptor.getReadMethod());
                        if (result == null){
                            continue;
                        }
                        if (result.toString().length() > stringColumn.length) {
                            String msg = StrUtil.isBlank(this.message)?DEFAULT_MESSAGE:this.message;
                            hibernateContext.buildConstraintViolationWithTemplate(StrUtil.format(msg, stringColumn.name))
                                    .addConstraintViolation();
                            return false;
                        }
                    }
                }

            }
            return true;
        }
    }

}