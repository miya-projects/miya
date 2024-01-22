package com.miya.system.module.dict;

import com.miya.common.module.base.BaseRepository;
import com.miya.common.module.base.DefaultQuerydslBinder;
import com.miya.system.module.dict.model.QSysDict;
import com.miya.system.module.dict.model.SysDict;
import com.querydsl.core.types.dsl.StringExpression;
import lombok.NonNull;
import org.springframework.data.querydsl.binding.QuerydslBindings;

public interface SysDictRepository extends BaseRepository<SysDict, QSysDict> {

    @Override
    default void customize(@NonNull QuerydslBindings bindings, @NonNull QSysDict qSysDict) {
        DefaultQuerydslBinder.customize(bindings, qSysDict);
        bindings.bind(qSysDict.code)
                .first(StringExpression::eq);
    }

}
