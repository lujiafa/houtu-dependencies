package com.houtu.monitor.autoconfigure;

import com.houtu.monitor.handler.*;
import com.houtu.monitor.prop.MonitorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jon
 * @date 2020年12月17日
 */
@Configuration
@ConditionalOnBean(MonitorWriter.class)
@EnableConfigurationProperties(MonitorProperties.class)
public class MonitorAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(name = {MonitorProperties.PREFIX + ".fullRequest",MonitorProperties.PREFIX + ".full-request"}, havingValue = "false", matchIfMissing = true)
    static class DefaultRequestMonitorConfiguration {
        @Bean
        @ConditionalOnMissingBean(ReqMonitorAspectHandler.class)
        public ReqMonitorAspectHandler reqMonitorAspectHandler() {
            return new ReqMonitorAspectHandler();
        }
    }

    @Configuration
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

    @Bean
    @ConditionalOnMissingBean(MonitorLog.class)
    public MonitorLog monitorLog() {
        return MonitorLog.getInstance();
    }

}
