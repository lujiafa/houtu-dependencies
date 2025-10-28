package com.houtu.springcloud.feign.consumer;

import feign.Client;
import feign.Request;
import org.slf4j.Logger;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;

/**
 * @Description 扩展FeignBlockingLoadBalancerClient
 * @Author jonlu
 * @Date 2020/5/25 14:03
 */
public class ExtensionFeignBlockingLoadBalancerClient extends FeignBlockingLoadBalancerClient {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ExtensionFeignBlockingLoadBalancerClient.class);

    public ExtensionFeignBlockingLoadBalancerClient(Client delegate, LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory) {
        super(delegate, loadBalancerClient, loadBalancerClientFactory);
    }

    @Override
    protected Request buildRequest(Request request, String reconstructedUrl) {
        if (logger.isDebugEnabled()) {
            logger.debug("FeignClient-buildRequest|{}|{}|{}", request.httpMethod().name(), request.url(), reconstructedUrl);
        }
        return super.buildRequest(request, reconstructedUrl);
    }
}
