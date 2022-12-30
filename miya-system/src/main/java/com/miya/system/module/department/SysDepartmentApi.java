package com.miya.system.module.department;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseApi;
import com.miya.system.module.department.dto.SysDepartmentDTO;
import com.miya.system.module.department.form.SysDepartmentForm;
import com.miya.system.module.user.model.SysUser;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 部门实现
 **/
@RequestMapping("department")
@RestController
@Slf4j
@Api(tags = {"部门"})
@Acl(userType = SysUser.class)
@Validated
public class SysDepartmentApi extends BaseApi {

    @Resource
    private SysDepartmentRepository sysDepartmentRepository;

    final QSysDepartment qSysDepartment = QSysDepartment.sysDepartment;

    /**
     * 部门列表
     */
    @ApiOperation("部门列表")
    @GetMapping
    public R<?> list(
            @QuerydslPredicate(root = SysDepartment.class) Predicate predicate,
            Pageable pageRequest) {
        Page<SysDepartment> all = sysDepartmentRepository.findAll(qSysDepartment.parent.isNull().and(predicate), pageRequest);
        return R.successWithData(Grid.of(all.map(SysDepartmentDTO::of)));
    }

    /**
     * 部门列表不分页
     */
    @ApiOperation("部门列表不分页")
    @GetMapping(params = "noPage")
    public R<?> listNoPage() {
        Iterable<SysDepartment> all = sysDepartmentRepository.findAll(qSysDepartment.parent.isNull());
        List<SysDepartmentDTO> list = StreamSupport.stream(all.spliterator(), false)
                .map(SysDepartmentDTO::of).collect(Collectors.toList());
        return R.successWithData(list);
    }

    /**
     * 新增部门
     */
    @PostMapping
    @ApiOperation("新增部门")
    public R<?> save(@Validated SysDepartmentForm departmentForm) {
        boolean exists = sysDepartmentRepository.exists(qSysDepartment.name.eq(departmentForm.getName()));
        if (exists) {
            return R.errorWithMsg("该部门已存在");
        }
        sysDepartmentRepository.save(departmentForm.mergeToNewPo());
        return R.success();
    }

    /**
     * 部门修改
     */
    @PutMapping
    @ApiOperation("修改部门")
    public R<?> update(@NotBlank String name, @NotBlank String description, @RequestParam(value = "parent", required = false) SysDepartment parent,
                       @NotNull @RequestParam("id") SysDepartment department) {
        if (Objects.nonNull(parent)){
            department.setParent(parent);
        }
        department.setName(name);
        department.setDescription(description);
        sysDepartmentRepository.save(department);
        return R.success();
    }

    /**
     * 部门详情
     */
    @GetMapping("{id}")
    @ApiOperation("部门详情")
    public R<?> detail(@PathVariable("id") SysDepartment department) {
        return R.successWithData(department);
    }

    /**
     * 删除部门
     */
    @ApiOperation("删除部门以及子部门")
    @DeleteMapping("{id}")
    public R<?> delete(@PathVariable("id") SysDepartment department) {
        sysDepartmentRepository.delete(department);
        //
        // try {
        //     deleteAndChildren(department);
        // } catch (CannotDeleteException e) {
        //     return R.errorWithCodeAndMsg(ResponseCode.Common.CANNOT_DELETE, "该部门或子部门下有设备");
        // }
        return R.success();
    }



}
