//package com.miya.system.module.search;
//
//import com.miya.system.module.user.model.SysUser;
//import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
//import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
//import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
//import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
//
///**
// * 编程式映射
// * 启用:
// * hibernate.search.mapping.configurer=class:com.miya.system.module.search.MySearchMappingConfigurer
// */
//public class MySearchMappingConfigurer implements HibernateOrmSearchMappingConfigurer {
//    @Override
//    public void configure(HibernateOrmMappingConfigurationContext context) {
//        ProgrammaticMappingConfigurationContext mapping = context.programmaticMapping();
//        TypeMappingStep bookMapping = mapping.type( SysUser.class );
//        bookMapping.indexed();
//        bookMapping.property( "title" )
//                .fullTextField().analyzer( "english" );
//    }
//}
