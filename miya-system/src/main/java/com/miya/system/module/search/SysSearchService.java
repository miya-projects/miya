//package com.miya.system.module.search;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.core.SearchResponse;
//import co.elastic.clients.elasticsearch.core.search.Hit;
//import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
//import com.miya.common.exception.ErrorMsgException;
//import com.miya.system.module.log.SysLog;
//import com.miya.system.module.user.model.SysUser;
//import lombok.extern.slf4j.Slf4j;
//import org.hibernate.search.engine.backend.common.DocumentReference;
//import org.hibernate.search.engine.search.projection.dsl.SearchProjectionFactory;
//import org.hibernate.search.engine.search.query.SearchResult;
//import org.hibernate.search.mapper.orm.Search;
//import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
//import org.hibernate.search.mapper.orm.scope.SearchScope;
//import org.hibernate.search.mapper.orm.session.SearchSession;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityManagerFactory;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * es搜索服务
// */
//@Slf4j
//public class SysSearchService {
//    private final EntityManager entityManager;
//    private EntityManagerFactory entityManagerFactory;
//    SearchSession searchSession;
//
//    private final ElasticsearchClient elasticsearchClient;
//
//    private List<Class<?>> types = Arrays.asList(SysLog.class, SysUser.class);
//    private String[] fields = new String[]{
//            "operationType", "content", "operatorName", "business"
//    };
//
//    public SysSearchService(EntityManager entityManager, ElasticsearchClient elasticsearchClient) {
//        log.info("search init");
//        this.entityManager = entityManager;
//        // SearchMapping mapping = Search.mapping(this.entityManagerFactory);
//        // Backend backend = mapping.backend();
//        // ElasticsearchBackend elasticsearchBackend = backend.unwrap(ElasticsearchBackend.class);
//        // RestClient client = elasticsearchBackend.client(RestClient.class);
//        // List<Node> nodes = client.getNodes();
//        this.searchSession = Search.session(entityManager);
//        this.elasticsearchClient = elasticsearchClient;
//    }
//
//
//    private final AtomicBoolean isInit = new AtomicBoolean(false);
//
//    /**
//     * 删除所有索引并重新初始化
//     */
//    public void init() throws InterruptedException {
//        if (isInit.get()) {
//            throw new ErrorMsgException("已经在初始化了");
//        }
//        isInit.set(true);
//        try {
//            MassIndexer indexer = searchSession.massIndexer(SysLog.class, SysUser.class)
//                    .idFetchSize( 500 )
//                    .batchSizeToLoadObjects( 100 )
//                    .transactionTimeout(60 * 30)
//                    .threadsToLoadObjects(7);
//
//            indexer.type( SysLog.class ).reindexOnly( "e.publicationYear <= 2100" );
//            indexer.type( SysUser.class ).reindexOnly( "e.birthDate < :birthDate" )
//                    .param( "birthDate", LocalDate.ofYearDay( 2100, 77 ) );
//            // indexer.dropAndCreateSchemaOnStart(true);
//            indexer.purgeAllOnStart(true);
//            indexer.startAndWait();
//        } finally {
//            isInit.set(false);
//        }
//    }
//
//    /**
//     * 全文检索
//     * @param q 搜索关键字
//     */
//    public SearchResult<?> query(String q) {
//        SearchSession session = Search.session(entityManager);
//        SearchScope<?> scope = session.scope(Arrays.asList(SysLog.class));
//        scope.predicate().match().field("").matching("").toPredicate();
//
//
//        SearchResult<List<?>> result = searchSession.search(types)
//                .select(f -> f.composite(
//                        // SearchDTO::new,
//                        f.field("content", String.class),
//                        f.field("operatorName", String.class)
//                ))
//                // .where(sp -> sp.matchAll())
//                .where(sp -> sp.match().fields(fields).matching(q))
//                .fetch(10);
//        long totalHitCount = result.total().hitCount();
//        List<List<?>> hits = result.hits();
//        log.info("查询到{}个", totalHitCount);
//        log.info(Arrays.toString(hits.toArray()));
//        return result;
//    }
//
//    public void documentRef(String q) {
//        SearchSession searchSession = Search.session(entityManager);
//        searchSession.search(Arrays.asList(SysLog.class))
//                .select(SearchProjectionFactory::documentReference)
//                .where(f -> f.match()
//                        .fields("operationType", "content", "operatorName", "business")
//                        .matching(q))
//                .fetchHits(20);
//
//
//
//        List<DocumentReference> hits = searchSession.search(SysLog.class)
//                .select(SearchProjectionFactory::documentReference)
//                .where(f -> f.match()
//                        .fields("operationType", "content", "operatorName", "business")
//                        .matching(q))
//                .fetchHits(20);
//    }
//
//
//    public void queryByNative(String a) throws IOException {
//        HitsMetadata<Map> hits = elasticsearchClient.search(s ->
//                s.query(
//                        q -> q.match(t -> t.field("name").queryName("Admin").query(f -> f.stringValue("Admin")))
//                ), Map.class).hits();
//        for (Hit<Map> hit : hits.hits()) {
//            log.info(hit.source().toString());
//        }
//
//        SearchResponse<SysUser> search = elasticsearchClient.search(s -> s
//                        .index("sysuser-000001")
//                        .query(q -> q
//                                .term(t -> t
//                                        .field("name")
//                                        .value(v -> v.stringValue("Admin"))
//                                )
//                        ),
//                SysUser.class);
//
//        for (Hit<SysUser> hit : search.hits().hits()) {
//            log.info(hit.source().toString());
//            // processProduct(hit.source());
//        }
//    }
//
//}
//
