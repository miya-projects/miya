package com.miya.system.module.dict;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.miya.common.annotation.Acl;
import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.base.BaseApi;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.model.dto.base.R;
import com.miya.system.module.dict.model.*;
import com.miya.system.module.user.model.SysUser;
import com.miya.system.module.dict.model.SysDictItem;
import com.miya.system.module.user.model.SysUserPrincipal;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequestMapping("/dict")
@RestController
@Slf4j
@Api(tags = {"字典"})
@Validated
@Acl(userType = SysUserPrincipal.class)
public class SysDictApi extends BaseApi {

    @Resource
    private SysDictRepository sysDictRepository;
    @Resource
    private SysDictItemRepository sysDictItemRepository;
    @Resource
    private SysDictService sysDictService;

    @ApiOperation("字典列表")
    @GetMapping
    @Acl(business = "sys:dict:list")
    public R<?> list(@QuerydslPredicate(root = SysDict.class) Predicate predicate, Pageable pageRequest) {
        Grid<SysDictDTO> grid = sysDictService.list(predicate, pageRequest);
        return R.successWithData(grid);
    }

    @ApiOperation("字典列表 不分页")
    @GetMapping(params = "noPage")
    public R<?> listNoPage() {
        return R.successWithData(sysDictService.listNoPage());
    }

    @ApiOperation("字典详情")
    @GetMapping("{id}")
    @Acl(business = "sys:dict:view")
    public R<SysDictDetailDTO> detail(@PathVariable(value = "id") SysDict sysDict,
                       @RequestParam(defaultValue = "", required = false) String code) {
        if (Objects.isNull(sysDict)){
            Optional<SysDictDetailDTO> dictByIdOrCode = sysDictService.getDictByCode(code);
            return dictByIdOrCode.map(R::successWithData).orElseGet(() -> R.errorWithMsg("id或code在数据库中不存在"));
        }
        return R.successWithData(SysDictDetailDTO.of(sysDict));
    }

    @PutMapping("{id}")
    @ApiOperation("更新字典")
    @Acl(business = "sys:dict:edit")
    public R<?> update(@Validated SysDictForm sysDictForm, @NotNull(message = "id不合法") @PathVariable(value = "id") SysDict sysDict) {
        sysDictForm.mergeToPo(sysDict);
        boolean existsBySysDict = sysDictRepository.exists(
                QSysDict.sysDict.code.eq(sysDict.getCode())
                .and(QSysDict.sysDict.id.ne(sysDict.getId()))
        );
        if (existsBySysDict) {
            return R.errorWithMsg("已经有这样的名字或code");
        }
        sysDictRepository.save(sysDict);
        return R.success();
    }

    @PostMapping
    @ApiOperation("新增字典")
    @Acl(business = "sys:dict:add")
    public R<?> save(@Validated SysDictForm sysDictForm) {
        SysDict sysDict = sysDictForm.mergeToNewPo();
        boolean exists = sysDictRepository.exists(QSysDict.sysDict.code.eq(sysDict.getCode())
                .or(QSysDict.sysDict.name.eq(sysDict.getName())));
        if (exists){
            return R.errorWithMsg("已经有这样的名字或code");
        }
        sysDict.setIsSystem(Boolean.FALSE);
        sysDictRepository.save(sysDict);
        return R.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation(value = "删除字典", notes = "删除字典以及该字典下所有的字典项")
    @Acl(business = "sys:dict:delete")
    public R<?> delete(@PathVariable(value = "id") SysDict sysDict) {
        sysDictService.deleteDict(sysDict);
        return R.success();
    }


//    ************************字典数据*********************************

    @GetMapping("{idOrCode}/item")
    @ApiOperation(value = "查询字典数据", notes = "id和code二选一")
    @Acl(business = "sys:dict:view")
    public R<?> dictItemList(
            @QuerydslPredicate(root = SysDictItem.class) Predicate predicate,
            @ApiParam("字典id") @PathVariable(value = "idOrCode") String idOrCode,
            @PageableDefault(sort = BaseEntity.Fields.createdTime, direction = Sort.Direction.DESC) Pageable pageable) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        Optional<SysDict> dictOptional = sysDictRepository.findOne(QSysDict.sysDict.code.eq(idOrCode).or(QSysDict.sysDict.id.eq(idOrCode)));
        if (!dictOptional.isPresent()) {
            return R.errorWithMsg("没有这样的code或id");
        }
        SysDict dict = dictOptional.get();
        predicate = qSysDictItem.sysDict.eq(dict).and(predicate);
        Page<SysDictItem> all = sysDictItemRepository.findAll(predicate, pageable);
        return R.successWithData(Grid.of(all.map(SysDictItemDTO::of)));
    }

    @GetMapping("{idOrCode}/item/noPage")
    @ApiOperation("查询字典数据不分页")
    public R<?> selectSysDictInfoList(
            @ApiParam("字典code") @PathVariable String idOrCode) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qSysDictItem.sysDict.code.eq(idOrCode).or(qSysDictItem.sysDict.id.eq(idOrCode)));
        Iterable<SysDictItem> all = sysDictItemRepository.findAll(builder);
        return R.successWithData(StreamSupport.stream(all.spliterator(), false).map(SysDictItemDTO::of).collect(Collectors.toList()));
    }

    @ApiOperation("新增字典数据")
    @PostMapping("{idOrCode}/item")
    @Acl(business = "sys:dict:add")
    public R<?> saveDictData(@Validated SysDictItemForm sysDictItemForm, @PathVariable("idOrCode") @NotNull String idOrCode) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        Optional<SysDict> dictOptional = sysDictRepository.findOne(QSysDict.sysDict.code.eq(idOrCode).or(QSysDict.sysDict.id.eq(idOrCode)));
        if (!dictOptional.isPresent()) {
            return R.errorWithMsg("找不到字典");
        }
        SysDict sysDict = dictOptional.get();
        boolean exists = sysDictItemRepository.exists(qSysDictItem.value.eq(sysDictItemForm.getValue()).and(qSysDictItem.sysDict.eq(sysDict)) );
        if (exists) {
            return R.errorWithMsg("value已经存在");
        }
        SysDictItem sysDictItem = sysDictItemForm.mergeToNewPo();
        sysDictItem.setSysDict(sysDict);
        sysDictItemRepository.save(sysDictItem);
        return R.success();
    }

    @ApiOperation("修改字典数据")
    @PutMapping("item/{id}")
    @Acl(business = "sys:dict:edit")
    public R<?> updateDictData(@Validated SysDictItemForm sysDictItemForm, @PathVariable("id") @NotNull SysDictItem sysDictItem) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        boolean exists = sysDictItemRepository.exists(qSysDictItem.value.eq(
                sysDictItemForm.getValue()).and(qSysDictItem.id.ne(sysDictItem.getId())));
        if (exists){
            return R.errorWithMsg("已经有这样的名字或code");
        }
        BeanUtil.copyProperties(sysDictItemForm.mergeToNewPo(), sysDictItem, CopyOptions.create().ignoreNullValue());
        sysDictItemRepository.save(sysDictItem);
        return R.success();
    }

    @DeleteMapping("item/{id}")
    @ApiOperation("删除字典数据")
    @Acl(business = "sys:dict:delete")
    public R<?> deleteDictData(@PathVariable("id") @NotNull SysDictItem sysDictItem) {
        sysDictItemRepository.delete(sysDictItem);
        return R.success();
    }

}
