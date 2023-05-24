//package com.miya.system.module.search.elastic;
//
//import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
//import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;
//
///**
// * hibernate-search分析器
// * to enable:
// * <property name="hibernate.search.backend.analysis.configurer"
// *           value="class:org.hibernate.search.documentation.gettingstarted.withhsearch.customanalysis.MyElasticsearchAnalysisConfigurer"/>
// */
//public class MyElasticsearchAnalysisConfigurer implements ElasticsearchAnalysisConfigurer {
//    @Override
//    public void configure(ElasticsearchAnalysisConfigurationContext context) {
//        context.analyzer( "english" ).custom()
//                .tokenizer( "standard" )
//                .tokenFilters( "lowercase", "snowball_english", "asciifolding" );
//
//        context.tokenFilter( "snowball_english" )
//                .type( "snowball" )
//                .param( "language", "English" );
//
//        context.analyzer( "name" ).custom()
//                .tokenizer( "standard" )
//                .tokenFilters( "lowercase", "asciifolding" );
//    }
//}
