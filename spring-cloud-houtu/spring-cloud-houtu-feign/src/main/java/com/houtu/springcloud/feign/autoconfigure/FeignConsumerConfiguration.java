package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.consumer.ExtensionFeignBlockingLoadBalancerClient;
import com.houtu.springcloud.feign.consumer.FeignDelegateDecoder;
import feign.Client;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;

public class FeignConsumerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Conditional({OnRetryNotEnabledCondition.class})
    public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        // 参考 DefaultFeignLoadBalancerConfiguration
        return new ExtensionFeignBlockingLoadBalancerClient(new Client.Default((SSLSocketFactory)null, (HostnameVerifier)null), loadBalancerClient, loadBalancerClientFactory, transformers);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(com.houtu.web.handler.UnifiedHandlerExceptionResolver.class)
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
                                ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        return new FeignDelegateDecoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters, customizers)));
    }

}
