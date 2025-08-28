package com.houtu.springcloud.feign.consumer;

import feign.Client;
import feign.Request;
import org.slf4j.Logger;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;

import java.util.List;

/**
 * @Description 扩展RetryableFeignBlockingLoadBalancerClient
 * @Author jonlu
 * @Date 2020/5/25 14:03
 */
public class ExtensionRetryableFeignBlockingLoadBalancerClient extends RetryableFeignBlockingLoadBalancerClient {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ExtensionRetryableFeignBlockingLoadBalancerClient.class);

    public ExtensionRetryableFeignBlockingLoadBalancerClient(Client delegate, LoadBalancerClient loadBalancerClient, LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerClientFactory loadBalancerClientFactory, List<LoadBalancerFeignRequestTransformer> transformers) {
        super(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory, transformers);
    }

    @Override
    protected Request buildRequest(Request request, String reconstructedUrl, ServiceInstance instance) {
        if (logger.isDebugEnabled()) {
            logger.debug("FeignClient-buildRequest|{}|{}|{}", request.httpMethod().name(), request.url(), reconstructedUrl);
        }
        return super.buildRequest(request, reconstructedUrl, instance);
    }
}
