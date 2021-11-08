package com.miya.system.annotation.constraint;

import com.miya.system.module.role.model.SysRoleForm;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
