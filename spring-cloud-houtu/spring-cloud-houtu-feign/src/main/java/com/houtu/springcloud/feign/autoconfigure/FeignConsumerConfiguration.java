package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.consumer.ExtensionFeignBlockingLoadBalancerClient;
import com.houtu.springcloud.feign.consumer.FeignSecurityRequestInterceptor;
import com.houtu.springcloud.feign.consumer.OnFeignSecurityRequestInterceptorCondition;
import com.houtu.springcloud.feign.consumer.prop.FeignConsumerProperties;
import feign.Client;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;

@EnableConfigurationProperties(FeignConsumerProperties.class)
public class FeignConsumerConfiguration {

    private FeignConsumerProperties feignConsumerProperties;

    public FeignConsumerConfiguration(ObjectProvider<FeignConsumerProperties> feignConsumerPropertiesProvider) {
        this.feignConsumerProperties = feignConsumerPropertiesProvider.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional({OnRetryNotEnabledCondition.class})
    public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        // 参考 DefaultFeignLoadBalancerConfiguration
        return new ExtensionFeignBlockingLoadBalancerClient(new Client.Default((SSLSocketFactory)null, (HostnameVerifier)null), loadBalancerClient, loadBalancerClientFactory, transformers);
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(OnFeignSecurityRequestInterceptorCondition.class)
    public FeignSecurityRequestInterceptor feignSecurityRequestInterceptor() {
        return new FeignSecurityRequestInterceptor(feignConsumerProperties);
    }

}
