package com.houtu.springcloud.feign.consumer.loadbalancer;

import com.houtu.springcloud.feign.consumer.ExtensionFeignBlockingLoadBalancerClient;
import com.houtu.springcloud.feign.consumer.ExtensionRetryableFeignBlockingLoadBalancerClient;
import feign.Client;
import feign.okhttp.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({OkHttpClient.class})
@ConditionalOnProperty({"spring.cloud.openfeign.okhttp.enabled"})
@ConditionalOnBean({LoadBalancerClient.class, LoadBalancerClientFactory.class})
@EnableConfigurationProperties({LoadBalancerClientsProperties.class})
public class ExtensionOkHttpFeignLoadBalancerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional({OnRetryNotEnabledCondition.class})
    public Client feignClient(okhttp3.OkHttpClient okHttpClient, LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        OkHttpClient delegate = new OkHttpClient(okHttpClient);
        return new ExtensionFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancerClientFactory, transformers);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {"org.springframework.retry.support.RetryTemplate"})
    @ConditionalOnBean({LoadBalancedRetryFactory.class})
    @ConditionalOnProperty(value = {"spring.cloud.loadbalancer.retry.enabled"}, havingValue = "true", matchIfMissing = true)
    public Client feignRetryClient(LoadBalancerClient loadBalancerClient, okhttp3.OkHttpClient okHttpClient, LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        OkHttpClient delegate = new OkHttpClient(okHttpClient);
        return new ExtensionRetryableFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory, transformers);
    }

}
