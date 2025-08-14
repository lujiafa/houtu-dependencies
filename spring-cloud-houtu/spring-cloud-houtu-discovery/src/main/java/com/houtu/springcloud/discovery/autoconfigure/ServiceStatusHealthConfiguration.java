package com.houtu.springcloud.discovery.autoconfigure;

import com.houtu.springcloud.discovery.health.ActuatorServiceStatusHealthIndicator;
import com.houtu.springcloud.discovery.health.ReactiveServiceHealthWebFilter;
import com.houtu.springcloud.discovery.health.WebMvcServiceHealthFilter;
import com.houtu.springcloud.discovery.constant.DiscoveryConstant;
import com.houtu.springcloud.discovery.context.ServiceContext;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.server.WebFilter;

import java.beans.Introspector;

public class ServiceStatusHealthConfiguration {

    @ConditionalOnClass(org.springframework.boot.actuate.health.HealthEndpoint.class)
    static class ActuatorServiceStatusHealthConfiguration {
        @Bean
        @ConditionalOnBean({ServiceContext.class})
        public ActuatorServiceStatusHealthIndicator actuatorServiceStatusHealthIndicator(ServiceContext serviceContext) {
            return new ActuatorServiceStatusHealthIndicator(serviceContext);
        }

    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class WebMvcServiceStatusHealthConfiguration {
        @Bean
        @ConditionalOnBean({ServiceContext.class})
        public FilterRegistrationBean<Filter> webMvcServiceHealthFilterFilterRegistrationBean(ServiceContext serviceContext) {
            FilterRegistrationBean<Filter> requestSerialRegistration = new FilterRegistrationBean();
            requestSerialRegistration.setFilter(new WebMvcServiceHealthFilter(serviceContext));
            requestSerialRegistration.addUrlPatterns(DiscoveryConstant.COMMON_HEALTH_PATH);
            requestSerialRegistration.setName(Introspector.decapitalize(WebMvcServiceHealthFilter.class.getSimpleName()));
            requestSerialRegistration.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return requestSerialRegistration;
        }
    }

    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveServiceStatusHealthConfiguration {
        @Bean
        @ConditionalOnBean({ServiceContext.class})
        public WebFilter reactiveServiceHealthFilterFilterRegistrationBean(ServiceContext serviceContext) {
            return new ReactiveServiceHealthWebFilter(serviceContext);
        }
    }
}
