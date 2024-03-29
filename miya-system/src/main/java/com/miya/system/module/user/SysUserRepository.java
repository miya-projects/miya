package com.miya.system.module.user;

import com.miya.common.module.base.BaseRepository;
import com.miya.common.module.base.DefaultQuerydslBinder;
import com.miya.system.module.user.model.QSysUser;
import com.miya.system.module.user.model.SysUser;
import jakarta.annotation.Nonnull;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.List;

/**
 * @author 杨超辉
 */
public interface SysUserRepository extends BaseRepository<SysUser, QSysUser> {

    @Override
    default void customize(@Nonnull QuerydslBindings bindings, @Nonnull QSysUser qSysUser) {
        DefaultQuerydslBinder.customize(bindings, qSysUser);
        bindings.excluding(qSysUser.password, qSysUser.id, qSysUser.avatar);
    }

    <T> List<T> findByName(String name,  Class<T> type);

}
