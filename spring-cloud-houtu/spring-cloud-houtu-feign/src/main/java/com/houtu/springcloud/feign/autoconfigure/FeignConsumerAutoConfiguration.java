package com.houtu.springcloud.feign.autoconfigure;

import com.houtu.springcloud.feign.consumer.FeignDelegateDecoder;
import com.houtu.springcloud.feign.consumer.loadbalancer.ExtensionDefaultFeignLoadBalancerConfiguration;
import com.houtu.springcloud.feign.consumer.loadbalancer.ExtensionHttpClient5FeignLoadBalancerConfiguration;
import com.houtu.springcloud.feign.consumer.loadbalancer.ExtensionOkHttpFeignLoadBalancerConfiguration;
import com.houtu.util.autoconfigure.UtilsAutoConfiguration;
import feign.Feign;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration(
        before = {FeignLoadBalancerAutoConfiguration.class, FeignAutoConfiguration.class},
        after = {UtilsAutoConfiguration.class, BlockingLoadBalancerClientAutoConfiguration.class, LoadBalancerAutoConfiguration.class}
)
@ConditionalOnClass({Feign.class, FeignClient.class})
@Import({ExtensionOkHttpFeignLoadBalancerConfiguration.class, ExtensionHttpClient5FeignLoadBalancerConfiguration.class, ExtensionDefaultFeignLoadBalancerConfiguration.class})
public class FeignConsumerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(com.houtu.web.handler.UnifiedHandlerExceptionResolver.class)
    public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
                                ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        // 参考：org.springframework.cloud.openfeign.FeignClientsConfiguration（通过FeignAutoConfiguration-FeignClientFactory动态实例化）
        return new FeignDelegateDecoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters, customizers)));
    }

}
