package com.houtu.springcloud.loadbalancer.autoconfigure;


import com.houtu.springcloud.loadbalancer.prop.SpringCloudLoadBalancerProperties;
import com.houtu.springcloud.loadbalancer.support.SpringCloudLoadBalancerClientConfiguration;
import com.houtu.springcloud.loadbalancer.support.hint.*;
import feign.Feign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Servlet;
import java.beans.Introspector;

/**
 * 参考：LoadBalancerClientConfiguration、NacosLoadBalancerClientConfiguration
 * @author: jonlu
 * @date: 2023/9/15
 */

@AutoConfiguration
@EnableConfigurationProperties(SpringCloudLoadBalancerProperties.class)
@AutoConfigureBefore({LoadBalancerAutoConfiguration.class, ReactorLoadBalancerClientAutoConfiguration.class, LoadBalancerBeanPostProcessorAutoConfiguration.class})
public class SpringCloudLoadBalancerAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(SpringCloudLoadBalancerAutoConfiguration.class);

    @Configuration
    @ConditionalOnWebApplication(
            type = ConditionalOnWebApplication.Type.SERVLET
    )
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean({WebMvcConfigurationSupport.class})
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class SpringMVCConfiguration {
        @Bean
        public FilterRegistrationBean<HintRequestFilter> hintRequestFilterRegistrationBean() {
            logger.info("Enable SpringMVC full-link hint function request filter.");
            FilterRegistrationBean<HintRequestFilter> requestSerialRegistration = new FilterRegistrationBean<HintRequestFilter>();
            requestSerialRegistration.setFilter(new HintRequestFilter());
            requestSerialRegistration.addUrlPatterns("/*");
            requestSerialRegistration.setName(Introspector.decapitalize(HintRequestAcrossThreadProcessor.class.getSimpleName()));
            requestSerialRegistration.setOrder(Ordered.LOWEST_PRECEDENCE);
            return requestSerialRegistration;
        }
    }

    @Configuration
    @ConditionalOnWebApplication(
            type = ConditionalOnWebApplication.Type.REACTIVE
    )
    @ConditionalOnClass({WebFluxConfigurer.class})
    @ConditionalOnMissingBean({WebFluxConfigurationSupport.class})
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class SpringWebFluxConfiguration {
        @Bean
        @ConditionalOnClass(RoutePredicateHandlerMapping.class)
        @ConditionalOnProperty(
                name = {"spring.cloud.gateway.enabled"},
                matchIfMissing = true
        )
        public WebFilter hintGatewayWebFilter(SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
            logger.info("Enable SpringCloudGateway hint request filter.");
            return new HintGatewayWebFilter(springCloudLoadBalancerProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "hintGatewayWebFilter",
                type = "com.houtu.springcloud.loadbalancer.support.hint.HintGatewayWebFilter")
        public WebFilter hintWebFilter() {
            logger.info("Enable SpringWebFlux full-link hint function request filter.");
            return new HintWebFilter();
        }
    }


    @Configuration
    @ConditionalOnClass(Feign.class)
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class HintFeignConfiguration {
        @Bean
        public HintFeignInterceptor hintFeignInterceptor() {
            logger.info("Enable full-link hint function Feign interceptor.");
            return new HintFeignInterceptor();
        }
    }

    @Configuration
    @LoadBalancerClients(
            defaultConfiguration = {SpringCloudLoadBalancerClientConfiguration.class}
    )
    public static class LoadBalancerConfiguration {
    }

}
