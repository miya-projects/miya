package com.miya.common.config.orm.annotations;

import com.miya.common.config.orm.NoDashesUUIDGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(NoDashesUUIDGenerator.class)
//@ValueGenerationType(generatedBy = NoDashesUUIDGenerator.class)
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface NoDashesUuidGenerator {


}
