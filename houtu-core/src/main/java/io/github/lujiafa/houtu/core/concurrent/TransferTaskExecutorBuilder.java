package io.github.lujiafa.houtu.core.concurrent;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TransferTaskExecutorBuilder extends ThreadPoolTaskExecutorBuilder {

    @Override
    public ThreadPoolTaskExecutor build() {
        return this.configure(new TransferThreadPoolTaskExecutor());
    }
}
