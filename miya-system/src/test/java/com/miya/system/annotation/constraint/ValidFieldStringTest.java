package com.miya.system.annotation.constraint;

import com.miya.system.module.role.model.SysRoleForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import java.util.Set;

@Slf4j
public class ValidFieldStringTest {

    @Test
    public void testValid(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        SysRoleForm sysRoleForm = new SysRoleForm();
        sysRoleForm.setName("123");
        Set<ConstraintViolation<SysRoleForm>> constraintViolations = validator.validate(sysRoleForm);
        constraintViolations.forEach( constraintViolation -> log.info(constraintViolation.getMessage()));
        log.info("finish");
//        assertEquals( 1, constraintViolations.size() );
//        assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
    }

}
