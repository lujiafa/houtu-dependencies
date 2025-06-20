package com.houtu.springcloud.loadbalancer.support;

import com.houtu.springcloud.loadbalancer.support.hint.HintBasedServiceInstanceListSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnDiscoveryEnabled
@Order(-1)
public class SpringCloudLoadBalancerClientConfiguration {
    private static final int REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER = 173827465;


    @ConditionalOnClass({com.alibaba.cloud.nacos.loadbalancer.LoadBalancerNacosAutoConfiguration.class})
    @com.alibaba.cloud.nacos.loadbalancer.ConditionalOnLoadBalancerNacos
    @com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled
    public static class NacosLoadBalancerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "nacosLoadBalancer")
        public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory,
                                                                      com.alibaba.cloud.nacos.NacosDiscoveryProperties nacosDiscoveryProperties) {
            String name = environment.getProperty("loadbalancer.client.name");
            return new NacosLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, nacosDiscoveryProperties);
        }
    }

    @Configuration(
            proxyBeanMethods = false
    )
    @ConditionalOnBlockingDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER + 1)
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class BlockingSupportConfiguration {
        public BlockingSupportConfiguration() {
        }

        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().with(buildHints()).withBlockingDiscoveryClient().build(context);
        }

        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().with(buildHints()).withBlockingDiscoveryClient().withZonePreference().build(context);
        }
    }

    @Configuration(
            proxyBeanMethods = false
    )
    @ConditionalOnReactiveDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER)
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class ReactiveSupportConfiguration {
        public ReactiveSupportConfiguration() {
        }

        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().with(buildHints()).withDiscoveryClient().build(context);
        }

        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().with(buildHints()).withDiscoveryClient().withZonePreference().build(context);
        }
    }

    private static ServiceInstanceListSupplierBuilder.DelegateCreator buildHints() {
        ServiceInstanceListSupplierBuilder.DelegateCreator creator = (context, delegate) -> {
            LoadBalancerClientFactory factory = (LoadBalancerClientFactory)context.getBean(LoadBalancerClientFactory.class);
            return new HintBasedServiceInstanceListSupplier(delegate, factory);
        };
        return creator;
    }
}
