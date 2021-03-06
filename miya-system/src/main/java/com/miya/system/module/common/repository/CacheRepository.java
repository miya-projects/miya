package com.miya.system.module.common.repository;

import com.miya.system.module.common.po.QSysCache;
import com.miya.system.module.common.po.SysCache;
import com.miya.common.module.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * @author 杨超辉
 * 持久化缓存
 */
public interface CacheRepository extends BaseRepository<SysCache, QSysCache> {

    /**
     * 清理过期的cache数据
     */
    @Query
    void deleteAllByExpireDateBefore(Date date);
}
