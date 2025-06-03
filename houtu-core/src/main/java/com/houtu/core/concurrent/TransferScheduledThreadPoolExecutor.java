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
        if (TRANSFER_LIST.size() > 0) {
            TransferThreadPoolExecutor.DelegatingRunnable runnable = (TransferThreadPoolExecutor.DelegatingRunnable) r;
            Map<Object, Object> transferMap = runnable.getAcrossMap();
            try {
                TRANSFER_LIST.forEach(processor -> {
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
        if (TRANSFER_LIST.size() > 0) {
            TransferThreadPoolExecutor.DelegatingRunnable runnable = (TransferThreadPoolExecutor.DelegatingRunnable) r;
            Map<Object, Object> transferMap = runnable.getAcrossMap();
            try {
                TRANSFER_LIST.forEach(transfer -> {
                    transfer.childExecuteAfter(runnable.getParent(), transferMap.get(transfer));
                });
            } finally {
                transferMap.clear();
            }
        }
    }

    @Override
    public void execute(Runnable command) {
        if (TRANSFER_LIST.size() > 0) {
            Map<Object, Object> acrossMap = new HashMap<>();
            TRANSFER_LIST.forEach(t -> {
                acrossMap.put(t, t.parentGet());
            });
            super.execute(new TransferThreadPoolExecutor.DelegatingRunnable(command, acrossMap));
            return;
        }
        super.execute(command);
    }

    static final List<AcrossThreadProcessor> TRANSFER_LIST = new ArrayList<>();
    static {
        ServiceLoader<AcrossThreadProcessor> serviceLoader = ServiceLoader.load(AcrossThreadProcessor.class);
        Iterator<AcrossThreadProcessor> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            TRANSFER_LIST.add(iterator.next());
        }
    }
}
