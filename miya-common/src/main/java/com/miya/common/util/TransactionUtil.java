package com.miya.common.util;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * 事务相关工具类
 */
@Component
public class TransactionUtil implements SmartInitializingSingleton {

    /**
     * 类自身实例
     */
    public static TransactionUtil INSTANCE;

    // @Autowired
    // public void setSelf(TransactionUtil self) {
    //     INSTANCE = self;
    // }


    // public TransactionUtil() {
    //     if (INSTANCE != null) {
    //         throw new IllegalStateException(this.getClass().toString() + "是单例，不可实例化多次");
    //     }
    //     INSTANCE = this;
    // }

    /**
     * 利用spring 机制和jdk8的Consumer机制实现只消费的事务
     */
    @Transactional(propagation = REQUIRES_NEW, rollbackFor = Exception.class) //可以根据实际业务情况，指定明确的回滚异常
    public void transactional(Runnable runnable) {

        runnable.run();
    }

    /**
     * 利用spring 机制和jdk8的Consumer机制实现只消费的事务
     */
    @Transactional(propagation = REQUIRES_NEW, rollbackFor = Exception.class) //可以根据实际业务情况，指定明确的回滚异常
    public <T> void transactional(Consumer<T> consumer, T t) {
        consumer.accept(t);
    }

    @Transactional(propagation = REQUIRES_NEW, rollbackFor = Exception.class) //可以根据实际业务情况，指定明确的回滚异常
    public <T, R> R transactional(Function<T, R> function, T t) {
        return function.apply(t);
    }

    @Transactional(propagation = REQUIRES_NEW, rollbackFor = Exception.class)
    public <R> R transactional(Supplier<R> function) {
        return function.get();
    }

    /**
     * TODO 待测试
     * <p>
     *     在结束事务后做一些事情<br />
     *     使用spring声明式事务时，想要在事务提交后做一些动作时，必须要将这样的逻辑写到方法外(因为方法调用结束才会提交事务)，
     *     这时如果这段逻辑想要封装在这个方法中，就可以工具类这个方法来注册这段逻辑，只会在事务提交后执行了
     * </p>
     * <br />
     * 调用例子:
     * <pre>
     *     &#64;Transactional
     *     public void doTx(){
     *         // start tx
     *         TransactionUtil.doAfterTransaction(() -> {
     *             // do something after tx
     *         });
     *         // end tx
     *     }
     * </pre>
     * @param runnable  事务提交后需要执行的逻辑
     */
    public static void doAfterTransaction(Runnable runnable){
        if (TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_COMMITTED) {
                        runnable.run();
                    }
                }
            });
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        INSTANCE = SpringUtil.getBean(TransactionUtil.class);
    }
}

