package com.miya.common.config.orm;

import cn.hutool.core.collection.ListUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
//import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.util.CastUtils;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  <a href="https://stackoverflow.com/questions/45843457/how-to-discover-fully-qualified-table-column-from-hibernate-metadatasources">参考</a>
 */
@Slf4j
public class MetadataExtractorIntegrator
        implements org.hibernate.integrator.spi.Integrator, IntegratorProvider {

    public static final MetadataExtractorIntegrator INSTANCE =
            new MetadataExtractorIntegrator();

    private Database database;

    private Metadata metadata;

    public Database getDatabase() {
        return database;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public void integrate(
            Metadata metadata,
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        this.database = metadata.getDatabase();
        this.metadata = metadata;

    }

    @Override
    public void disintegrate(
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {

    }


    /*
        key为实体类class，value为该实体类的columns对象集(只有sqlType为StringType的column)
     */
    public static Map<Class<?>, List<PropertyLength>> CACHE_MAP;

    /**
     * 获取一个PO类的数据库字段信息
     */
    public List<PropertyLength> getStringColumns(Class<?> entityClass) {
        if (CACHE_MAP == null) {
            initCache();
        }
        return CACHE_MAP.get(entityClass);
    }

    /**
     * 获取一个PO类的数据库字段信息
     */
    public PropertyLength getStringColumns(Class<?> entityClass, String propertyName) {
        List<PropertyLength> stringColumns = getStringColumns(entityClass);
        return stringColumns.stream()
                .filter(propertyLength -> propertyLength.name.equals(propertyName)).findFirst().orElse(null);
    }


    /**
     * 初始化缓存
     */
    private void initCache() {
        CACHE_MAP = new HashMap<>();

        Metadata metadata = MetadataExtractorIntegrator.INSTANCE.getMetadata();
        if (metadata == null){

            throw new IllegalArgumentException("MetadataExtractorIntegrator未初始化");
        }
        Collection<PersistentClass> entityBindings = metadata.getEntityBindings();

        for (PersistentClass persistentClass : entityBindings) {
            Class<?> mappedClass = persistentClass.getMappedClass();
            // Table table = persistentClass.getTable();
            Iterator<Property> propertyIterator = CastUtils.cast(persistentClass.getPropertyIterator());
            ArrayList<Property> properties = ListUtil.toList(propertyIterator);

            // todo test
            List<PropertyLength> columnList = properties.stream()
                    .filter(property -> property.getType().getClass().equals(StringJavaType.class))
                    // .filter(property -> {
                    //     Iterator<Column> columnIterator = CastUtils.cast(property.getColumnIterator());
                    //     ArrayList<Column> columns = ListUtil.toList(columnIterator);
                    //     return columns.size() != 0;
                    // })
                    .map(property -> {
                        Iterator<Column> columnIterator = CastUtils.cast(property.getColumnIterator());
                        ArrayList<Column> columns = ListUtil.toList(columnIterator);
                        if (columns.size() > 1) {
                            log.info("是谁有2个column?");
                        }
                        return new PropertyLength(property.getName(), columns.get(0).getLength());
                    })
                    .collect(Collectors.toList());
            CACHE_MAP.put(mappedClass, columnList);
        }
    }

    @Override
    public List<Integrator> getIntegrators() {
        return Collections.singletonList(MetadataExtractorIntegrator.INSTANCE);
    }

    @AllArgsConstructor
    public static class PropertyLength{
        public final String name;
        public final long length;
    }

}
