package com.houtu.springcloud.loadbalancer.support;

import com.houtu.springcloud.loadbalancer.support.condition.DefaultLoadBalancerCondition;
import com.houtu.springcloud.loadbalancer.support.condition.NacosLoadBalancerCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;


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

    @Configuration(proxyBeanMethods = false)
    @Conditional(NacosLoadBalancerCondition.class)
    public static class SpringCloudNacosLoadBalancerConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ReactorLoadBalancer<ServiceInstance> springCloudLoadBalancer(Environment environment,
                                                                            LoadBalancerClientFactory loadBalancerClientFactory,
                                                                            com.alibaba.cloud.nacos.NacosDiscoveryProperties nacosDiscoveryProperties) {
            String name = environment.getProperty("loadbalancer.client.name");
            String clusterName = StringUtils.isEmpty(nacosDiscoveryProperties.getClusterName()) ? "default" : nacosDiscoveryProperties.getClusterName();
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
            String clusterName = environment.getProperty("spring.cloud.discovery.cluster-name");
            if (StringUtils.isEmpty(clusterName)) {
                clusterName = "default";
            }
            return new SpringCloudLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, clusterName);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBlockingDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER + 1)
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class BlockingSupportConfiguration {
        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().with(SpringCloudDelegateCreator.build()).withWeighted(SpringCloudWeightFunction.build()).build(context);
        }

        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().withZonePreference().with(SpringCloudDelegateCreator.build()).withWeighted(SpringCloudWeightFunction.build()).build(context);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnReactiveDiscoveryEnabled
    @Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER)
    @ConditionalOnProperty(name = "spring.cloud.loadbalancer.hint.enable", havingValue = "true", matchIfMissing = true)
    public static class ReactiveSupportConfiguration {
        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withDiscoveryClient().with(SpringCloudDelegateCreator.build()).withWeighted(SpringCloudWeightFunction.build()).build(context);
        }

        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder().withDiscoveryClient().withZonePreference().with(SpringCloudDelegateCreator.build()).withWeighted(SpringCloudWeightFunction.build()).build(context);
        }
    }

}
