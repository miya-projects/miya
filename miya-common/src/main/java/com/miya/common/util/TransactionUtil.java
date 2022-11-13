package com.miya.common.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务相关工具类
 */
public class TransactionUtil {


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

}

