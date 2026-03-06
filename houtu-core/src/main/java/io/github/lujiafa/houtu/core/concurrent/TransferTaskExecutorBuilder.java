package io.github.lujiafa.houtu.core.concurrent;

import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TransferTaskExecutorBuilder extends TaskExecutorBuilder {

    @Override
    public ThreadPoolTaskExecutor build() {
        return this.configure(new TransferThreadPoolTaskExecutor());
    }
}
