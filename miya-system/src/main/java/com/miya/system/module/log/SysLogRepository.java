package com.miya.system.module.log;

import com.miya.common.module.base.DefaultQuerydslBinder;
import com.miya.common.module.base.BaseRepository;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import jakarta.annotation.Nonnull;


/**
 * @author 杨超辉
 * 日志
 */
public interface SysLogRepository extends BaseRepository<SysLog, QSysLog> {

    @Override
    default void customize(@Nonnull QuerydslBindings bindings, @Nonnull QSysLog qSysLog) {
        DefaultQuerydslBinder.customize(bindings, qSysLog);
    }

}
