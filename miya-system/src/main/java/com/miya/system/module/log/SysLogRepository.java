package com.miya.system.module.log;

import com.miya.common.module.base.BaseRepository;
import com.miya.common.module.base.DefaultQuerydslBinder;
import com.querydsl.core.types.dsl.SimpleExpression;
import jakarta.annotation.Nonnull;
import org.springframework.data.querydsl.binding.QuerydslBindings;


/**
 * @author 杨超辉
 * 日志
 */
public interface SysLogRepository extends BaseRepository<SysLog, QSysLog> {

    @Override
    default void customize(@Nonnull QuerydslBindings bindings, @Nonnull QSysLog qSysLog) {
        DefaultQuerydslBinder.customize(bindings, qSysLog);
        bindings.bind(qSysLog.businessId).first(SimpleExpression::eq);
    }

}
