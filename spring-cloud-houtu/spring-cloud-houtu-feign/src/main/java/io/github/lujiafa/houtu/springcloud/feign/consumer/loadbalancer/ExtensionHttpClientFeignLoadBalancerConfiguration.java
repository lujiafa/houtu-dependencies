package io.github.lujiafa.houtu.springcloud.feign.consumer.loadbalancer;

import io.github.lujiafa.houtu.springcloud.feign.consumer.ExtensionFeignBlockingLoadBalancerClient;
import io.github.lujiafa.houtu.springcloud.feign.consumer.ExtensionRetryableFeignBlockingLoadBalancerClient;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import org.apache.http.client.HttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.HttpClient5DisabledConditions;
import org.springframework.cloud.openfeign.clientconfig.HttpClientFeignConfiguration;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnClass({ApacheHttpClient.class})
@ConditionalOnBean({LoadBalancerClient.class, LoadBalancerClientFactory.class})
@ConditionalOnProperty(value = {"feign.httpclient.enabled"})
@Conditional({HttpClient5DisabledConditions.class})
@Import({HttpClientFeignConfiguration.class})
@EnableConfigurationProperties({LoadBalancerClientsProperties.class})
public class ExtensionHttpClientFeignLoadBalancerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional({OnRetryNotEnabledCondition.class})
    public Client feignClient(LoadBalancerClient loadBalancerClient, HttpClient httpClient, LoadBalancerClientFactory loadBalancerClientFactory) {
        // 参考 org.springframework.cloud.openfeign.loadbalancer.HttpClientFeignLoadBalancerConfiguration
        ApacheHttpClient delegate = new ApacheHttpClient(httpClient);
        return new ExtensionFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancerClientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {"org.springframework.retry.support.RetryTemplate"})
    @ConditionalOnBean({LoadBalancedRetryFactory.class})
    @ConditionalOnProperty(value = {"spring.cloud.loadbalancer.retry.enabled"}, havingValue = "true", matchIfMissing = true)
    public Client feignRetryClient(LoadBalancerClient loadBalancerClient, HttpClient httpClient, LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerClientFactory loadBalancerClientFactory) {
        ApacheHttpClient delegate = new ApacheHttpClient(httpClient);
        return new ExtensionRetryableFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory);
    }

}
