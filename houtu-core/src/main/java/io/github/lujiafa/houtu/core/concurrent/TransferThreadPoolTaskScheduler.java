package io.github.lujiafa.houtu.core.concurrent;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2021年5月19日
 * @Description 线程池任务调度执行器
 *  注：具备实现父、子线程数据传递
 */
public class TransferThreadPoolTaskScheduler extends ThreadPoolTaskScheduler {

    @Override
    protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        return new TransferScheduledThreadPoolExecutor(poolSize, threadFactory, rejectedExecutionHandler);
    }
}
