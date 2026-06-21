package io.github.lujiafa.houtu.core.autoconfigure;

import io.github.lujiafa.houtu.core.concurrent.TransferTaskExecutorBuilder;
import io.github.lujiafa.houtu.core.concurrent.TransferTaskSchedulerBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@AutoConfiguration
@EnableConfigurationProperties({TaskExecutionProperties.class, TaskSchedulingProperties.class})
@AutoConfigureBefore(value = {TaskExecutionAutoConfiguration.class, TaskSchedulingAutoConfiguration.class})
public class CoreTaskExecutionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ThreadPoolTaskExecutor.class})
    public TaskExecutorBuilder threadPoolTaskExecutorBuilder(TaskExecutionProperties properties, ObjectProvider<TaskExecutorCustomizer> threadPoolTaskExecutorCustomizers, ObjectProvider<TaskDecorator> taskDecorator) {
        TaskExecutionProperties.Pool pool = properties.getPool();
        TaskExecutionProperties.Shutdown shutdown = properties.getShutdown();
        return new TransferTaskExecutorBuilder(new TaskExecutorBuilder()
                .queueCapacity(pool.getQueueCapacity())
                .corePoolSize(pool.getCoreSize())
                .maxPoolSize(pool.getMaxSize())
                .allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout())
                .keepAlive(pool.getKeepAlive())
                .awaitTermination(shutdown.isAwaitTermination())
                .awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod())
                .threadNamePrefix(properties.getThreadNamePrefix())
                .customizers(threadPoolTaskExecutorCustomizers.orderedStream()::iterator)
                .taskDecorator((TaskDecorator) taskDecorator.getIfUnique()));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({ThreadPoolTaskScheduler.class})
    public TaskSchedulerBuilder threadPoolTaskSchedulerBuilder(TaskSchedulingProperties properties, ObjectProvider<TaskSchedulerCustomizer> threadPoolTaskSchedulerCustomizers) {
        TaskSchedulingProperties.Shutdown shutdown = properties.getShutdown();
        return new TransferTaskSchedulerBuilder(new TaskSchedulerBuilder()
                .poolSize(properties.getPool().getSize())
                .awaitTermination(shutdown.isAwaitTermination())
                .awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod())
                .threadNamePrefix(properties.getThreadNamePrefix())
                .customizers(threadPoolTaskSchedulerCustomizers));
    }

}
