package io.github.lujiafa.houtu.springcloud.loadbalancer.support;

import io.github.lujiafa.houtu.springcloud.loadbalancer.prop.SpringCloudLoadBalancerProperties;
import io.github.lujiafa.houtu.springcloud.loadbalancer.support.condition.DefaultLoadBalancerCondition;
import io.github.lujiafa.houtu.springcloud.loadbalancer.support.condition.EnabledWeightCondition;
import io.github.lujiafa.houtu.springcloud.loadbalancer.support.condition.NacosLoadBalancerCondition;
import io.github.lujiafa.houtu.util.constant.CharConstant;
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
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;


/**
 * 参考：
 * org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration
 * com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancerClientConfiguration
 *
 * @author: jonlu
 * @date: 2021/7/27
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpringCloudLoadBalancerClientConfiguration {
    private static final int REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER = 173827465;

    @Bean
    @Conditional(EnabledWeightCondition.class)
    public SpringCloudLoadBalancerWeightLifecycle springCloudLoadBalancerWeightLifecycle() {
        return new SpringCloudLoadBalancerWeightLifecycle();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(com.alibaba.cloud.nacos.NacosDiscoveryProperties.class)
    @Conditional(NacosLoadBalancerCondition.class)
    public static class SpringCloudNacosLoadBalancerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ReactorLoadBalancer<ServiceInstance> springCloudLoadBalancer(Environment environment,
                                                                            LoadBalancerClientFactory loadBalancerClientFactory,
                                                                            com.alibaba.cloud.nacos.NacosDiscoveryProperties nacosDiscoveryProperties) {
            String name = environment.getProperty("loadbalancer.client.name");
            String clusterName = nacosDiscoveryProperties.getClusterName() == null ? CharConstant.EMPTY : nacosDiscoveryProperties.getClusterName();
            return new SpringCloudLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, clusterName, (serviceInstance -> serviceInstance.getMetadata().get("nacos.cluster")));
        }

    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(DefaultLoadBalancerCondition.class)
    public static class SpringCloudLoadBalancerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ReactorLoadBalancer<ServiceInstance> springCloudLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
            String name = environment.getProperty("loadbalancer.client.name");
            String clusterName = environment.getProperty("spring.cloud.discovery.cluster-name", CharConstant.EMPTY);
            return new SpringCloudLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, clusterName);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBlockingDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER + 1)
    public static class BlockingSupportConfiguration {
        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context, SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
            ServiceInstanceListSupplierBuilder serviceInstanceListSupplierBuilder = ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().withCaching();
            if (springCloudLoadBalancerProperties.isHint()) {
                serviceInstanceListSupplierBuilder.with(SpringCloudHintDelegateCreator.build());
            }
            if (springCloudLoadBalancerProperties.isWeight()) {
                serviceInstanceListSupplierBuilder.withWeighted(SpringCloudWeightFunction.build());
            }
            return serviceInstanceListSupplierBuilder.build(context);
        }

        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context, SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
            ServiceInstanceListSupplierBuilder serviceInstanceListSupplierBuilder = ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().withCaching().withZonePreference();
            if (springCloudLoadBalancerProperties.isHint()) {
                serviceInstanceListSupplierBuilder.with(SpringCloudHintDelegateCreator.build());
            }
            if (springCloudLoadBalancerProperties.isWeight()) {
                serviceInstanceListSupplierBuilder.withWeighted(SpringCloudWeightFunction.build());
            }
            return serviceInstanceListSupplierBuilder.build(context);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnReactiveDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER)
    public static class ReactiveSupportConfiguration {
        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context, SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
            ServiceInstanceListSupplierBuilder serviceInstanceListSupplierBuilder = ServiceInstanceListSupplier.builder().withDiscoveryClient();
            if (springCloudLoadBalancerProperties.isHint()) {
                serviceInstanceListSupplierBuilder.with(SpringCloudHintDelegateCreator.build());
            }
            if (springCloudLoadBalancerProperties.isWeight()) {
                serviceInstanceListSupplierBuilder.withWeighted(SpringCloudWeightFunction.build());
            }
            return serviceInstanceListSupplierBuilder.build(context);
        }

        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context, SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
            ServiceInstanceListSupplierBuilder serviceInstanceListSupplierBuilder = ServiceInstanceListSupplier.builder().withDiscoveryClient().withZonePreference();
            if (springCloudLoadBalancerProperties.isHint()) {
                serviceInstanceListSupplierBuilder.with(SpringCloudHintDelegateCreator.build());
            }
            if (springCloudLoadBalancerProperties.isWeight()) {
                serviceInstanceListSupplierBuilder.withWeighted(SpringCloudWeightFunction.build());
            }
            return serviceInstanceListSupplierBuilder.build(context);
        }
    }

}
