package com.miya.system.module.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.miya.common.exception.ErrorMsgException;
import com.miya.system.module.log.SysLog;
import com.miya.system.module.user.model.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.hibernate.search.backend.elasticsearch.ElasticsearchBackend;
import org.hibernate.search.backend.elasticsearch.search.projection.impl.ElasticsearchFieldProjection;
import org.hibernate.search.engine.backend.Backend;
import org.hibernate.search.engine.backend.common.DocumentReference;
import org.hibernate.search.engine.search.common.ValueConvert;
import org.hibernate.search.engine.search.projection.dsl.SearchProjectionFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * es搜索服务
 */
@Slf4j
public class SysSearchService {
    private final EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    SearchSession searchSession;

    private final ElasticsearchClient elasticsearchClient;

    private List<Class<?>> types = Arrays.asList(SysLog.class, SysUser.class);
    private String[] fields = new String[]{
            "operationType", "content", "operatorName", "business"
    };

    public SysSearchService(EntityManager entityManager, ElasticsearchClient elasticsearchClient) {
        log.info("search init");
        this.entityManager = entityManager;
        // SearchMapping mapping = Search.mapping(this.entityManagerFactory);
        // Backend backend = mapping.backend();
        // ElasticsearchBackend elasticsearchBackend = backend.unwrap(ElasticsearchBackend.class);
        // RestClient client = elasticsearchBackend.client(RestClient.class);
        // List<Node> nodes = client.getNodes();
        this.searchSession = Search.session(entityManager);
        this.elasticsearchClient = elasticsearchClient;
    }


    private final AtomicBoolean isInit = new AtomicBoolean(false);

    /**
     * 初始化数据
     */
    public void init() throws InterruptedException {
        if (isInit.get()) {
            throw new ErrorMsgException("已经在初始化了");
        }
        isInit.set(true);
        try {
            MassIndexer indexer = searchSession.massIndexer(SysLog.class, SysUser.class)
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
        searchSession.search(Arrays.asList(SysLog.class))
                .select(SearchProjectionFactory::documentReference)
                .where(f -> f.match()
                        .fields("operationType", "content", "operatorName", "business")
                        .matching(q))
                .fetchHits(20);



        List<DocumentReference> hits = searchSession.search(SysLog.class)
                .select(SearchProjectionFactory::documentReference)
                .where(f -> f.match()
                        .fields("operationType", "content", "operatorName", "business")
                        .matching(q))
                .fetchHits(20);
    }


    public void queryByNative(String a) throws IOException {
        HitsMetadata<Map> hits = elasticsearchClient.search(s ->
                s.query(
                        q -> q.match(t -> t.field("name").queryName("Admin").query(f -> f.stringValue("Admin")))
                ), Map.class).hits();
        for (Hit<Map> hit : hits.hits()) {
            log.info(hit.source().toString());
        }

        SearchResponse<SysUser> search = elasticsearchClient.search(s -> s
                        .index("sysuser-000001")
                        .query(q -> q
                                .term(t -> t
                                        .field("name")
                                        .value(v -> v.stringValue("Admin"))
                                )
                        ),
                SysUser.class);

        for (Hit<SysUser> hit : search.hits().hits()) {
            log.info(hit.source().toString());
            // processProduct(hit.source());
        }
    }

}

