package com.miya.common.config;

import cn.hutool.core.thread.ExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;

/**
 * 线程池配置
 * 例子代码
 */
@Configuration
public class ThreadPoolConfig {


//    @Bean(name = "taskExecutor")
    // @ConditionalOnMissingBean(Executor.class)
    public Executor taskExecutor(){
        // todo 动态调整
        float blockingCoefficient = 0.5f;
        // 最佳的线程数 = CPU可用核心数 / (1 - 阻塞系数)
        int poolSize = (int) (Runtime.getRuntime().availableProcessors() / (1 - blockingCoefficient));
        ExecutorBuilder executorBuilder = ExecutorBuilder.create().setCorePoolSize(poolSize)
                .setMaxPoolSize(poolSize).setKeepAliveTime(60L);
        executorBuilder.setWorkQueue(new ArrayBlockingQueue<>(200))
                .setAllowCoreThreadTimeOut(true);
        return executorBuilder.build();
        // //定制线程名称，还可以定制线程group
        // executor.setThreadFactory(new ThreadFactory() {
        //     private final AtomicLong threadNumber = new AtomicLong(1);
        //     @Override
        //     public Thread newThread(Runnable r) {
        //         return new Thread(Thread.currentThread().getThreadGroup(), r,
        //                 "async-eventListener-" + threadNumber.getAndIncrement(),
        //                 0);
        //     }
        // });
        // executor.setCorePoolSize(10);
        // executor.setAllowCoreThreadTimeOut(true);
        // executor.setMaxPoolSize(20);
        // executor.setKeepAliveSeconds(5);
        // executor.setQueueCapacity(100);
        // executor.setRejectedExecutionHandler(null);
        // return executor;
    }
}
