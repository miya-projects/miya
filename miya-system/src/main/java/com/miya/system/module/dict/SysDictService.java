package com.miya.system.module.dict;

import com.google.common.collect.Lists;
import com.miya.common.model.dto.base.Grid;
import com.miya.common.module.base.BaseEntity;
import com.miya.common.module.base.BaseService;
import com.miya.system.module.dict.model.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * 获取某个字典的所有值
     * @param code 字典code
     */
    public List<SysDictItem> getDictValues(String code){
        Iterable<SysDictItem> all = sysDictItemRepository.findAll(QSysDictItem.sysDictItem.sysDict.code.eq(code));
        return Lists.newArrayList(all.iterator());
    }

    /**
     * 删除字典和数据
     * @param sysDict
     */
    // public void deleteDictAndData(SysDict sysDict) {
    //     Iterable<SysDictItem> all = sysDictItemRepository.findAll(QSysDictItem.sysDictItem.sysDict.eq(sysDict));
    //     sysDictItemRepository.deleteInBatch(all);
    //     sysDictRepository.delete(sysDict);
    // }


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
     * 不分页查询数据字典
     */
    public List<SysDictDTO> listNoPage() {
        List<SysDict> all = sysDictRepository.findAll(Sort.by(Sort.Order.desc(BaseEntity.Fields.createdTime)));
        return all.stream().map(SysDictDTO::of).collect(Collectors.toList());
    }

    /**
     * 根据id或code查找字典
     * @param code
     */
    public Optional<SysDictDetailDTO> getDictByCode(String code) {
        QSysDict qSysDict = QSysDict.sysDict;
        SysDictDetailDTO sysDictDetailDTO = qf.select(Projections.bean(SysDictDetailDTO.class, new QSysDictDetailDTO(qSysDict)))
                .from(qSysDict).where(qSysDict.code.eq(code)).fetchOne();
        return Optional.ofNullable(sysDictDetailDTO);
    }

    /**
     * 删除字典和字典项
     * @param sysDict
     */
    public void deleteDict(SysDict sysDict) {
        sysDictRepository.delete(sysDict);
    }
}
