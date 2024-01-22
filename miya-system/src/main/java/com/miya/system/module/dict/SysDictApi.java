package com.miya.system.module.dict;

import com.miya.common.annotation.Acl;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.common.module.base.BaseApi;
import com.miya.common.module.base.BaseEntity;
import com.miya.system.module.dict.model.*;
import com.miya.system.module.user.model.SysUserPrincipal;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/dict")
@RestController
@Slf4j
@Tag(name = "字典")
@Validated
@Acl(userType = SysUserPrincipal.class)
@RequiredArgsConstructor
public class SysDictApi extends BaseApi {

    private final SysDictService sysDictService;

    @Operation(summary = "字典列表")
    @GetMapping
    @Acl(business = "sys:dict:list")
    public R<Grid<SysDictDTO>> list(@QuerydslPredicate(root = SysDict.class) Predicate predicate,
                                    @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC)  Pageable pageable) {
        return R.successWithData(sysDictService.list(predicate, pageable));
    }

    @Operation(summary = "字典详情")
    @GetMapping("{id}")
    @Acl(business = "sys:dict:view")
    public R<SysDictDetailDTO> detail(@PathVariable(value = "id") SysDict sysDict) {
        return R.successWithData(SysDictDetailDTO.of(sysDict));
    }

    @PostMapping
    @Operation(summary = "新增字典")
    @Acl(business = "sys:dict:add")
    public R<?> save(@Validated SysDictForm sysDictForm) {
        sysDictService.saveDict(sysDictForm);
        return R.success();
    }

    @PutMapping("{id}")
    @Operation(summary = "更新字典")
    @Acl(business = "sys:dict:edit")
    public R<?> update(@Validated SysDictForm sysDictForm, @PathVariable(value = "id") SysDict sysDict) {
        sysDictService.updateDict(sysDictForm, sysDict);
        return R.success();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除字典", description = "删除字典以及该字典下所有的字典项")
    @Acl(business = "sys:dict:delete")
    public R<?> delete(@PathVariable(value = "id") SysDict sysDict) {
        sysDictService.deleteDict(sysDict);
        return R.success();
    }


    //    ************************字典数据*********************************

    @GetMapping("{id}/item")
    @Operation(summary = "字典数据列表")
    @Acl(business = "sys:dict:view")
    public R<Grid<SysDictItemDTO>> itemPage(
            @QuerydslPredicate(root = SysDictItem.class) Predicate predicate,
            @Parameter(description = "字典id") @PathVariable(value = "id") SysDict dict,
            @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC) Pageable pageable) {
        return R.successWithData(sysDictService.pageForDictItem(predicate, dict, pageable));
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("{id}/item")
    @Acl(business = "sys:dict:add")
    public R<?> saveDictData(@Validated SysDictItemForm sysDictItemForm, @PathVariable("id") @NotNull SysDict sysDict) {
        sysDictService.saveDictItem(sysDictItemForm, sysDict);
        return R.success();
    }

    @Operation(summary = "字典数据详情")
    @GetMapping("item/{id}")
    @Acl(business = "sys:dict:view")
    public R<SysDictItemDTO> detail(@PathVariable(value = "id") SysDictItem sysDictItem) {
        return R.successWithData(SysDictItemDTO.of(sysDictItem));
    }

    @Operation(summary = "修改字典数据")
    @PutMapping("item/{id}")
    @Acl(business = "sys:dict:edit")
    public R<?> updateDictData(@Validated SysDictItemForm sysDictItemForm, @PathVariable("id") @NotNull SysDictItem sysDictItem) {
        sysDictService.updateDictData(sysDictItemForm, sysDictItem);
        return R.success();
    }

    @DeleteMapping("item/{id}")
    @Operation(summary = "删除字典数据")
    @Acl(business = "sys:dict:delete")
    public R<?> deleteDictData(@PathVariable("id") @NotNull SysDictItem sysDictItem) {
        sysDictService.deleteDictData(sysDictItem);
        return R.success();
    }

}
