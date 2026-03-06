package io.github.lujiafa.houtu.springcloud.feign.consumer;

import feign.Client;
import feign.Request;
import org.slf4j.Logger;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;

/**
 * @Description 扩展RetryableFeignBlockingLoadBalancerClient
 * @Author jonlu
 * @Date 2020/5/25 14:03
 */
public class ExtensionRetryableFeignBlockingLoadBalancerClient extends RetryableFeignBlockingLoadBalancerClient {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ExtensionRetryableFeignBlockingLoadBalancerClient.class);

    public ExtensionRetryableFeignBlockingLoadBalancerClient(Client delegate, LoadBalancerClient loadBalancerClient, LoadBalancedRetryFactory loadBalancedRetryFactory, LoadBalancerClientFactory loadBalancerClientFactory) {
        super(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory);
    }

    @Override
    protected Request buildRequest(Request request, String reconstructedUrl) {
        if (logger.isDebugEnabled()) {
            logger.debug("FeignClient-buildRequest|{}|{}|{}", request.httpMethod().name(), request.url(), reconstructedUrl);
        }
        return super.buildRequest(request, reconstructedUrl);
    }
}
