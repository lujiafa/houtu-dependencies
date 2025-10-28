package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.consumer.ExtensionFeignBlockingLoadBalancerClient;
import com.houtu.springcloud.feign.consumer.ExtensionRetryableFeignBlockingLoadBalancerClient;
import com.houtu.springcloud.feign.consumer.FeignDelegateDecoder;
import feign.Client;
import feign.Feign;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

@AutoConfiguration(
        before = {FeignLoadBalancerAutoConfiguration.class, FeignAutoConfiguration.class},
        after = {BlockingLoadBalancerClientAutoConfiguration.class, LoadBalancerAutoConfiguration.class}
)
@ConditionalOnClass({Feign.class, FeignClient.class})
public class FeignConsumerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional({OnRetryNotEnabledCondition.class})
    public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory) {
        // 参考 DefaultFeignLoadBalancerConfiguration
        return new ExtensionFeignBlockingLoadBalancerClient(new Client.Default((SSLSocketFactory) null, (HostnameVerifier) null), loadBalancerClient, loadBalancerClientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {"org.springframework.retry.support.RetryTemplate"})
    @ConditionalOnBean({LoadBalancedRetryFactory.class})
    @ConditionalOnProperty(value = {"spring.cloud.loadbalancer.retry.enabled"}, havingValue = "true", matchIfMissing = true)
    public Client feignRetryClient(LoadBalancerClient loadBalancerClient, LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerClientFactory loadBalancerClientFactory) {
        return new ExtensionRetryableFeignBlockingLoadBalancerClient(new Client.Default((SSLSocketFactory) null, (HostnameVerifier) null), loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(com.houtu.web.handler.UnifiedHandlerExceptionResolver.class)
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
                                ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        // 参考：org.springframework.cloud.openfeign.FeignClientsConfiguration（通过FeignAutoConfiguration-FeignClientFactory动态实例化）
        return new FeignDelegateDecoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters, customizers)));
    }

}
