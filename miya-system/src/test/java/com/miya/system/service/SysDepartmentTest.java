package com.miya.system.service;

import com.miya.common.module.config.SysConfigService;
import com.miya.system.module.department.SysDepartment;
import com.miya.system.module.department.SysDepartmentRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SysDepartmentTest {

    @Resource
    private SysDepartmentRepository departmentRepository;


    @Test
    public void haha(){
        SysDepartment department = new SysDepartment();
        department.setId("11");
        department.setName("11");

        SysDepartment sub = new SysDepartment();
        sub.setId("22");
        sub.setName("22");

        department.setChildren(Arrays.asList(sub));

        departmentRepository.save(department);
    }
}
