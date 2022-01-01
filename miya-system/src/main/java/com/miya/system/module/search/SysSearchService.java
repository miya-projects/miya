package com.miya.system.module.search;

import com.miya.common.exception.ErrorMsgException;
import com.miya.system.module.log.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.backend.elasticsearch.search.projection.impl.ElasticsearchFieldProjection;
import org.hibernate.search.engine.backend.common.DocumentReference;
import org.hibernate.search.engine.search.common.ValueConvert;
import org.hibernate.search.engine.search.projection.dsl.SearchProjectionFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * es搜索服务
 */
@Slf4j
public class SysSearchService {
    private final EntityManager entityManager;
    SearchSession searchSession;

    private List<Class<?>> types = Arrays.asList(SysLog.class);
    private String[] fields = new String[]{
            "operationType", "content", "operatorName", "business"
    };

    public SysSearchService(EntityManager entityManager) {
        log.info("search init");
        this.entityManager = entityManager;
        this.searchSession = Search.session(entityManager);
    }

    private final AtomicBoolean isInit = new AtomicBoolean(false);

    /**
     * 初始化数据
     * @throws InterruptedException
     */
    public void init() throws InterruptedException {
        if (isInit.get()) {
            throw new ErrorMsgException("已经在初始化了");
        }
        isInit.set(true);
        try {
            Thread.sleep(10000L);
            MassIndexer indexer = searchSession.massIndexer(SysLog.class)
                    .threadsToLoadObjects(7);
            indexer.startAndWait();
        } finally {
            isInit.set(false);
        }
    }

    /**
     * 全文检索
     * @param q 搜索关键字
     */
    public SearchResult<?> query(String q) {
        SearchResult<List<?>> result = searchSession.search(types)
                .select(f -> f.composite(
                        // SearchDTO::new,
                        f.field("content", String.class),
                        f.field("operatorName", String.class)
                ))
                .where(sp -> sp.match().fields(fields).matching(q))
                .fetch(10);
        long totalHitCount = result.total().hitCount();
        List<List<?>> hits = result.hits();
        log.info("查询到{}个", totalHitCount);
        log.info(Arrays.toString(hits.toArray()));
        return result;
    }

    public void documentRef(String q) {
        SearchSession searchSession = Search.session(entityManager);
        List<DocumentReference> hits = searchSession.search(SysLog.class)
                .select(SearchProjectionFactory::documentReference)
                .where(f -> f.match()
                        .fields("operationType", "content", "operatorName", "business")
                        .matching(q))
                .fetchHits(20);
    }

}
