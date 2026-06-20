package io.github.lujiafa.houtu.springcloud.feign.autoconfigure;

import io.github.lujiafa.houtu.springcloud.feign.consumer.loadbalancer.ExtensionDefaultFeignLoadBalancerConfiguration;
import io.github.lujiafa.houtu.springcloud.feign.consumer.loadbalancer.ExtensionHttpClient5FeignLoadBalancerConfiguration;
import io.github.lujiafa.houtu.springcloud.feign.consumer.loadbalancer.ExtensionHttpClientFeignLoadBalancerConfiguration;
import io.github.lujiafa.houtu.springcloud.feign.consumer.loadbalancer.ExtensionOkHttpFeignLoadBalancerConfiguration;
import io.github.lujiafa.houtu.util.autoconfigure.UtilsAutoConfiguration;
import feign.Feign;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration(
        before = {FeignLoadBalancerAutoConfiguration.class, FeignAutoConfiguration.class},
        after = {UtilsAutoConfiguration.class, BlockingLoadBalancerClientAutoConfiguration.class, LoadBalancerAutoConfiguration.class}
)
@ConditionalOnClass({Feign.class, FeignClient.class})
@Import({ExtensionHttpClientFeignLoadBalancerConfiguration.class, ExtensionOkHttpFeignLoadBalancerConfiguration.class, ExtensionHttpClient5FeignLoadBalancerConfiguration.class, ExtensionDefaultFeignLoadBalancerConfiguration.class})
public class FeignConsumerAutoConfiguration {

}
