package com.miya.common.annotation.constraint;

import cn.hutool.extra.spring.SpringUtil;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Objects;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.validation.constraintvalidation.ValidationTarget.PARAMETERS;

/**
 * 保证该id在数据库内存在
 * 该类使用到spring容器，无法在spring容器外进行测试
 * 使用@NotNull + @RequestParam 可以达到同样的效果，并减少一次数据库查询
 * eg: \@NotNull(message = "id不合法") @RequestParam(value = "id") SysDict sysDict
 */
@Documented
@Constraint(validatedBy = { Id.IdValidator.class})
@SupportedValidationTarget({ValidationTarget.ANNOTATED_ELEMENT, PARAMETERS})
@Target({ FIELD, PARAMETER,ANNOTATION_TYPE })
@Retention(RUNTIME)
@NotBlank(message = "{propertyName}不能为空")
@Deprecated
public @interface Id {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};

    /**
     * 数据库表对应model类型
     * @return
     */
    Class<?> type();

    /**
     * id校验器
     */
    @Slf4j
    class IdValidator implements ConstraintValidator<Id, String> {

        private String message;
        Class<?> type;
        private JPAQueryFactory jpaQueryFactory;

        @Override
        public void initialize(Id constraintAnnotation) {
            this.type = constraintAnnotation.type();
            this.message = constraintAnnotation.message();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if(StringUtils.isBlank(this.message)){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{propertyName}:没有这样的id:'${validatedValue}'")
                        .addConstraintViolation();
            }
            if (Objects.isNull(this.jpaQueryFactory)){
                this.jpaQueryFactory = SpringUtil.getBean(JPAQueryFactory.class);
            }
            if(Objects.isNull(value)){
                return false;
            }
            SimpleEntityPathResolver simpleEntityPathResolver = SimpleEntityPathResolver.INSTANCE;
            EntityPath<?> path = simpleEntityPathResolver.createPath(this.type);
            BeanPath<?> idPath = new BeanPath<>(this.type, "id");
            JPAQuery<Long> query = jpaQueryFactory.select(Expressions.numberOperation(Long.class, Ops.AggOps.COUNT_AGG, path))
                    .from(path).where(Expressions.booleanOperation(Ops.EQ, idPath, ConstantImpl.create(value)));
            return !Long.valueOf(0).equals(query.fetchOne());
        }
    }

}
