package com.miya.common.module.bod;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.EmptyInterceptor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PostRemove;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 删除的数据，会备份到该数据库中
 * 关于逻辑删除: 真实世界没有真正的删除，只是不想要看到而已，谨慎考虑是要delete还是update status
 * todo 大量数据删除时会不会有性能问题? 待测试，如果有可以通过内存异步或kafka等队列系统解耦
 * <a href="https://www.infoq.cn/article/2009/09/Do-Not-Delete-Data/">不要删除数据</a>
 */
@Slf4j
public class BackupDataListener extends EmptyInterceptor {

    private EntityManagerFactory entityManagerFactory;
    /**
     * 是否开启删除时备份，找不到数据源会自动关闭
     */
    private Boolean enableBackup = true;

    /**
     * 是否需要备份的标记
     * key为entity class，value为true时表示需要备份
     */
    private final Map<Class<?>, Boolean> CACHE_MAP = new ConcurrentHashMap<>();

    @PostRemove
    void postRemove(Object entity) {
        if (!enableBackup){
            return;
        }
        Class<?> entityClass = entity.getClass();

        if (!isBackup(entityClass)){
            return;
        }

        EntityManagerFactory entityManagerFactory = getEntityManagerFactory();
        if (entityManagerFactory == null){
            return;
        }
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        // todo 报错
        entityManager.merge(entity);
        transaction.commit();
        entityManager.close();
    }

    /**
     * 是否需要备份删除的数据
     * @param entityClass
     * @return
     */
    private boolean isBackup(Class<?> entityClass) {
        return CACHE_MAP.computeIfAbsent(entityClass, key -> {
            BackupOnDelete backupOnDelete = AnnotationUtil.getAnnotation(entityClass, BackupOnDelete.class);
            return backupOnDelete != null;
        });

    }

    private EntityManagerFactory getEntityManagerFactory(){
        if (this.entityManagerFactory == null){
            try {
                this.entityManagerFactory = SpringUtil.getBean("backupEntityManagerFactory", EntityManagerFactory.class);
            }catch (NoSuchBeanDefinitionException e){
                // ignore
                enableBackup = false;
            }
        }
        return this.entityManagerFactory;
    }

}
