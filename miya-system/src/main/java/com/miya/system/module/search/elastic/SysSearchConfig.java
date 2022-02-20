package com.miya.system.module.search.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.miya.system.module.search.SysSearchApi;
import com.miya.system.module.search.SysSearchService;
import com.miya.system.module.search.elastic.ElasticConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.persistence.EntityManager;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "config.es", name = "hosts")
@EnableConfigurationProperties(ElasticConfigProperties.class)
public class SysSearchConfig implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("search config init");
    }

    /**
     * 区别于hibernate-session的另外的连接客户端
     * @param elasticConfigProperties   es配置
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticConfigProperties elasticConfigProperties){
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticConfigProperties.getUsername(), elasticConfigProperties.getPassword()));

        RestClient restClient = RestClient.builder(
                HttpHost.create(elasticConfigProperties.getProtocol() + "://" + elasticConfigProperties.getHosts()))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    @Bean
    public SysSearchService searchService(EntityManager entityManager, ElasticsearchClient elasticsearchClient){
        return new SysSearchService(entityManager, elasticsearchClient);
    }

    @Bean
    public SysSearchApi searchApi(SysSearchService searchService){
        return new SysSearchApi(searchService);
    }
}
