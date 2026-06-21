package io.github.lujiafa.houtu.core.concurrent;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 委托式 builder：Spring 的 {@link ThreadPoolTaskExecutorBuilder} 是不可变 builder，
 * 每个链式方法都返回基类新实例，若仅重写 build() 子类会在第一次链式调用后被丢弃。
 * 故持有已配置好的基类 builder，build() 时委托其 configure() 装配 Transfer 执行器。
 */
public class TransferTaskExecutorBuilder extends ThreadPoolTaskExecutorBuilder {

    private final ThreadPoolTaskExecutorBuilder delegate;

    public TransferTaskExecutorBuilder(ThreadPoolTaskExecutorBuilder delegate) {
        this.delegate = delegate;
    }

    @Override
    public ThreadPoolTaskExecutor build() {
        return delegate.configure(new TransferThreadPoolTaskExecutor());
    }
}
