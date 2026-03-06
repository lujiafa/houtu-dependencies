package io.github.lujiafa.houtu.springcloud.discovery.autoconfigure;

import io.github.lujiafa.houtu.springcloud.discovery.context.ServiceContext;
import io.github.lujiafa.houtu.springcloud.discovery.health.ActuatorServiceStatusHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

public class ServiceStatusHealthConfiguration {

    @ConditionalOnClass(org.springframework.boot.actuate.health.HealthEndpoint.class)
    static class ActuatorServiceStatusHealthConfiguration {
        @Bean
        @ConditionalOnBean({ServiceContext.class})
        public ActuatorServiceStatusHealthIndicator actuatorServiceStatusHealthIndicator(ServiceContext serviceContext) {
            return new ActuatorServiceStatusHealthIndicator(serviceContext);
        }

    }

}
