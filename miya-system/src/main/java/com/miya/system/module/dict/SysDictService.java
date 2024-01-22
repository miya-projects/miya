package com.miya.system.module.dict;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.miya.common.exception.ErrorMsgException;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.module.base.BaseService;
import com.miya.system.module.dict.model.*;
import com.querydsl.core.types.Predicate;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 字典服务
 */
@Service
public class SysDictService extends BaseService {

    @Resource
    private SysDictItemRepository sysDictItemRepository;
    @Resource
    private SysDictRepository sysDictRepository;

    /**
     * 分页查询数据字典
     * @param predicate
     * @param pageRequest
     */
    public Grid<SysDictDTO> list(Predicate predicate, Pageable pageRequest) {
        Page<SysDict> all = sysDictRepository.findAll(predicate, pageRequest);
        Page<SysDictDTO> page = all.map(SysDictDTO::of);
        return Grid.of(page);
    }

    /**
     * 删除字典和字典项
     */
    public void deleteDict(SysDict sysDict) {
        sysDictRepository.delete(sysDict);
    }

    /**
     * 更新字典
     */
    public void updateDict(SysDictForm sysDictForm, SysDict sysDict) {
        sysDictForm.mergeToPo(sysDict);
        boolean existsBySysDict = sysDictRepository.exists(
                QSysDict.sysDict.code.eq(sysDict.getCode())
                        .and(QSysDict.sysDict.id.ne(sysDict.getId()))
        );
        if (existsBySysDict) {
            throw new ErrorMsgException("已经有这样的code");
        }
        sysDictRepository.save(sysDict);
    }

    /**
     * 新增字典
     */
    public void saveDict(SysDictForm sysDictForm) {
        SysDict sysDict = sysDictForm.mergeToNewPo();
        boolean exists = sysDictRepository.exists(QSysDict.sysDict.code.eq(sysDict.getCode()));
        if (exists){
            throw new ErrorMsgException("已经有这样的code");
        }
        sysDict.setIsSystem(Boolean.FALSE);
        sysDictRepository.save(sysDict);
    }

    /**
     * 字典数据分页
     */
    public Grid<SysDictItemDTO> pageForDictItem(Predicate predicate, SysDict sysDict, Pageable pageable) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        predicate = qSysDictItem.sysDict.eq(sysDict).and(predicate);
        Page<SysDictItem> all = sysDictItemRepository.findAll(predicate, pageable);
        return Grid.of(all.map(SysDictItemDTO::of));
    }

    /**
     * 新增字典数据
     */
    public void saveDictItem(SysDictItemForm sysDictItemForm, @NotNull SysDict sysDict) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        boolean exists = sysDictItemRepository.exists(qSysDictItem.value.eq(sysDictItemForm.getValue()).and(qSysDictItem.sysDict.eq(sysDict)) );
        if (exists) {
            throw new ErrorMsgException("value已经存在");
        }
        SysDictItem sysDictItem = sysDictItemForm.mergeToNewPo();
        sysDictItem.setSysDict(sysDict);
        sysDictItemRepository.save(sysDictItem);
    }

    /**
     * 更新字典数据
     * @param sysDictItemForm
     * @param sysDictItem
     */
    public void updateDictData(SysDictItemForm sysDictItemForm, SysDictItem sysDictItem) {
        QSysDictItem qSysDictItem = QSysDictItem.sysDictItem;
        boolean exists = sysDictItemRepository.exists(qSysDictItem.value.eq(
                sysDictItemForm.getValue()).and(qSysDictItem.id.ne(sysDictItem.getId())));
        if (exists){
            throw new ErrorMsgException("已经有这样的code");
        }
        BeanUtil.copyProperties(sysDictItemForm.mergeToNewPo(), sysDictItem, CopyOptions.create().ignoreNullValue());
        sysDictItemRepository.save(sysDictItem);
    }

    /**
     * 删除字典数据
     */
    public void deleteDictData(SysDictItem sysDictItem) {
        sysDictItemRepository.delete(sysDictItem);
    }
}
