package com.houtu.monitor.autoconfigure;

import com.houtu.monitor.handler.ReqMonitorAspectHandler;
import com.houtu.monitor.handler.RequestFeignMonitorHandler;
import com.houtu.monitor.handler.RequestMonitorHandler;
import com.houtu.monitor.handler.RpcMonitorAspectHandler;
import com.houtu.monitor.prop.MonitorProperties;
import com.houtu.monitor.util.MonitorLog;
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
