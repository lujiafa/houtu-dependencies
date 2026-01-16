package com.houtu.core.concurrent;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2021年5月19日
 * @Description 线程池执行器
 *  注：具备实现父、子线程数据传递
 */
public class TransferThreadPoolExecutor extends ThreadPoolExecutor {
    public TransferThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public TransferThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TransferThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TransferThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        List<AcrossThreadProcessor> acrossThreadProcessors = AcrossThreadProcessorSupport.getAcrossThreadProcessors();
        if (!acrossThreadProcessors.isEmpty()) {
            DelegatingRunnable runnable = (DelegatingRunnable) r;
            Map<Object, Object> transferMap = runnable.getAcrossMap();
            try {
                acrossThreadProcessors.forEach(processor -> {
                    processor.childExecuteBefore(runnable.getParent(), transferMap.get(processor));
                });
            } catch (Throwable e) {
                transferMap.clear();
                throw e;
            }
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        List<AcrossThreadProcessor> acrossThreadProcessors = AcrossThreadProcessorSupport.getAcrossThreadProcessors();
        if (!acrossThreadProcessors.isEmpty()) {
            DelegatingRunnable runnable = (DelegatingRunnable) r;
            Map<Object, Object> transferMap = runnable.getAcrossMap();
            try {
                acrossThreadProcessors.forEach(transfer -> {
                    transfer.childExecuteAfter(runnable.getParent(), transferMap.get(transfer));
                });
            } finally {
                transferMap.clear();
            }
        }
    }

    @Override
    public void execute(Runnable command) {
        List<AcrossThreadProcessor> acrossThreadProcessors = AcrossThreadProcessorSupport.getAcrossThreadProcessors();
        if (!acrossThreadProcessors.isEmpty()) {
            Map<Object, Object> acrossMap = new HashMap<>();
            acrossThreadProcessors.forEach(t -> {
                acrossMap.put(t, t.parentGet());
            });
            super.execute(new DelegatingRunnable(command, acrossMap));
            return;
        }
        super.execute(command);
    }

}
