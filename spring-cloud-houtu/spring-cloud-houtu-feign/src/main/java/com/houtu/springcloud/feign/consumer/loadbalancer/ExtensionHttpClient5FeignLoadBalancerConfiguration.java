package com.houtu.springcloud.feign.consumer.loadbalancer;

import feign.Client;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.clientconfig.HttpClient5FeignConfiguration;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ApacheHttp5Client.class})
@ConditionalOnBean({LoadBalancerClient.class, LoadBalancerClientFactory.class})
@ConditionalOnProperty(value = {"spring.cloud.openfeign.httpclient.hc5.enabled"}, matchIfMissing = true)
@Import({ExtensionHttpClient5FeignLoadBalancerConfiguration.ExtensionHttpClient5FeignConfiguration.class})
@EnableConfigurationProperties({LoadBalancerClientsProperties.class})
public class ExtensionHttpClient5FeignLoadBalancerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional({OnRetryNotEnabledCondition.class})
    public Client feignClient(LoadBalancerClient loadBalancerClient, HttpClient httpClient5, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        Client delegate = new ApacheHttp5Client(httpClient5);
        return new FeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancerClientFactory, transformers);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = {"org.springframework.retry.support.RetryTemplate"})
    @ConditionalOnBean({LoadBalancedRetryFactory.class})
    @ConditionalOnProperty(value = {"spring.cloud.loadbalancer.retry.enabled"}, havingValue = "true", matchIfMissing = true)
    public Client feignRetryClient(LoadBalancerClient loadBalancerClient, HttpClient httpClient5, LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        Client delegate = new ApacheHttp5Client(httpClient5);
        return new RetryableFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory, transformers);
    }

    @Configuration
    @EnableConfigurationProperties(FeignHttpClientProperties.class)
    @ConditionalOnMissingBean(CloseableHttpClient.class)
    @ConditionalOnProperty(value = {"feign.httpclient.hc5.client.enabled"}, havingValue = "true", matchIfMissing = true)
    public static class ExtensionHttpClient5FeignConfiguration extends HttpClient5FeignConfiguration {

        @Bean
        @Override
        public CloseableHttpClient httpClient5(HttpClientConnectionManager hc5ConnectionManager, FeignHttpClientProperties httpClientProperties, ObjectProvider<List<HttpClientBuilderCustomizer>> customizerProvider) {
            return super.httpClient5(hc5ConnectionManager, httpClientProperties, customizerProvider);
        }
    }

}
