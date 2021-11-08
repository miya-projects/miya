package com.miya.system.module.notice;

import com.miya.common.module.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface SysNoticeUserRepository extends BaseRepository<SysNoticeUser, QSysNoticeUser> {

    @Transactional
    @Modifying
    @Query("update SysNoticeUser set read = true where id.sysUser.id = ?1")
    void updateReadByUser(String userId);
}
