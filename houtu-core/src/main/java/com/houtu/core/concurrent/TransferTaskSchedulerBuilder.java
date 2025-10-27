package com.houtu.core.concurrent;

import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TransferTaskSchedulerBuilder extends TaskSchedulerBuilder {

    @Override
    public ThreadPoolTaskScheduler build() {
        return this.configure(new TransferThreadPoolTaskScheduler());
    }
}
