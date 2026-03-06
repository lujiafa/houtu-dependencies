package io.github.lujiafa.houtu.core.concurrent;

import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2021年5月19日
 * @Description 实现线程池的任务执行器
 * 注：具备实现父、子线程数据传递
 */
public class TransferThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    protected ExecutorService initializeExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        BlockingQueue<Runnable> queue = this.createQueue(super.getQueueCapacity());
        ThreadPoolExecutor executor;
        TaskDecorator taskDecorator = getField(this, "taskDecorator", TaskDecorator.class);
        if (taskDecorator != null) {
            Map<Runnable, Object> decoratedTaskMap = getField(this, "decoratedTaskMap", Map.class);
            executor = new TransferThreadPoolExecutor(super.getCorePoolSize(), super.getMaxPoolSize(), super.getKeepAliveSeconds(), TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler) {
                public void execute(Runnable command) {
                    Runnable decorated = taskDecorator.decorate(command);
                    if (decorated != command) {
                        decoratedTaskMap.put(decorated, command);
                    }

                    super.execute(decorated);
                }
            };
        } else {
            executor = new TransferThreadPoolExecutor(super.getCorePoolSize(), super.getMaxPoolSize(), super.getKeepAliveSeconds(), TimeUnit.SECONDS, queue, threadFactory, rejectedExecutionHandler);
        }

        boolean allowCoreThreadTimeOut = getField(this, "allowCoreThreadTimeOut", Boolean.class);
        if (allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true);
        }

        boolean prestartAllCoreThreads = getField(this, "prestartAllCoreThreads", Boolean.class);
        if (prestartAllCoreThreads) {
            executor.prestartAllCoreThreads();
        }

        setField(this, "threadPoolExecutor", executor);
        return executor;
    }

    <T> T getField(Object object, String argName, Class<T> argType) {
        Field field = ReflectionUtils.findField(object.getClass(), argName);
        if (field == null) {
            return null;
        }
        boolean accessible = field.isAccessible();
        try {
            if (!accessible) {
                field.setAccessible(true);
            }
            return (T) ReflectionUtils.getField(field, object);
        } finally {
            field.setAccessible(accessible);
        }
    }

    <T> void setField(Object object, String argName, Object value) {
        Field field = ReflectionUtils.findField(object.getClass(), argName);
        if (field == null) {
            return;
        }
        boolean accessible = field.isAccessible();
        try {
            if (!accessible) {
                field.setAccessible(true);
            }
            ReflectionUtils.setField(field, object, value);
        } finally {
            field.setAccessible(accessible);
        }
    }
}
