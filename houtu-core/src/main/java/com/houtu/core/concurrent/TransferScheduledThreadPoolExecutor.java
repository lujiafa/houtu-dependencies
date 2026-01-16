package com.houtu.core.concurrent;

import java.util.*;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2021年5月19日
 * @Description 线程池调度执行器
 *  注：具备实现父、子线程数据传递
 */
public class TransferScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    public TransferScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public TransferScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public TransferScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public TransferScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
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
