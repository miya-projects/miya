package com.miya.system.module.dict;

import com.miya.common.module.base.BaseRepository;
import com.miya.system.module.dict.model.QSysDict;
import com.miya.system.module.dict.model.SysDict;

import java.util.Optional;

public interface SysDictRepository extends BaseRepository<SysDict, QSysDict> {

    Optional<SysDict> findByCode(String code);
}
