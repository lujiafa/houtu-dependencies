package com.houtu.monitor.autoconfigure;

import com.houtu.monitor.handler.*;
import com.houtu.monitor.handler.metric.RequestMetricProcessor;
import com.houtu.monitor.handler.metric.RpcMetricProcessor;
import com.houtu.monitor.prop.MonitorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @author jon
 * @date 2020年12月17日
 */
@Configuration
@ConditionalOnBean(MonitorWriter.class)
@EnableConfigurationProperties(MonitorProperties.class)
public class MonitorAutoConfiguration {

    private MonitorProperties monitorProperties;

    public MonitorAutoConfiguration(MonitorProperties monitorProperties) {
        this.monitorProperties = monitorProperties;
    }

    @Bean
    @ConditionalOnMissingBean(MonitorLog.class)
    @ConditionalOnBean(MonitorWriter.class)
    public MonitorLog monitorLog(Environment environment,
                                 List<MetricProcessor> metricProcessors,
                                 MonitorWriter monitorWriter) {
        String applicationName = environment.getProperty("spring.application.name", "");
        metricProcessors.add(new RequestMetricProcessor());
        metricProcessors.add(new RpcMetricProcessor());
        return new MonitorLog(monitorProperties.getBusinessName(),
                applicationName,
                monitorProperties.getPeriod().toMillis(),
                monitorProperties.getDelay().toMillis(),
                monitorProperties.getCollectQueueCapacity(),
                monitorProperties.getOutputQueueCapacity(),
                metricProcessors, monitorWriter);
    }

    @Configuration
    @ConditionalOnProperty(name = {MonitorProperties.PREFIX + ".fullRequest",MonitorProperties.PREFIX + ".full-request"}, havingValue = "false", matchIfMissing = true)
    static class AspectRequestMonitorConfiguration {
        @Bean
        @ConditionalOnMissingBean(ReqMonitorAspectHandler.class)
        public ReqMonitorAspectHandler requestMonitorAspectHandler() {
            return new ReqMonitorAspectHandler();
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(name = {MonitorProperties.PREFIX + ".fullRequest",MonitorProperties.PREFIX + ".full-request"}, havingValue = "true")
    static class FullRequestMonitorConfiguration {
        @Bean
        @ConditionalOnMissingBean(RequestFeignMonitorHandler.class)
        public RequestFeignMonitorHandler requestFeignMonitorHandler() {
            return new RequestFeignMonitorHandler();
        }

        @Bean
        @ConditionalOnMissingBean(RequestMonitorHandler.class)
        public RequestMonitorHandler requestMonitorHandler() {
            return new RequestMonitorHandler();
        }
    }

    @Bean
    @ConditionalOnMissingBean(RpcMonitorAspectHandler.class)
    public RpcMonitorAspectHandler rpcMonitorAspectHandler() {
        return new RpcMonitorAspectHandler();
    }

}
