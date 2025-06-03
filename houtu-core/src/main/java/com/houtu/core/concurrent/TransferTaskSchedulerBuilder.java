package com.houtu.core.concurrent;

import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

public class TransferTaskSchedulerBuilder extends ThreadPoolTaskSchedulerBuilder {

    @Override
    public ThreadPoolTaskScheduler build() {
        return this.configure(new TransferThreadPoolTaskScheduler());
    }
}
