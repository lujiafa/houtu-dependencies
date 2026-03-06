package io.github.lujiafa.houtu.springcloud.loadbalancer.support;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;

public class SpringCloudLoadBalancerWeightLifecycle implements LoadBalancerLifecycle<Object, Object, ServiceInstance> {
    @Override
    public boolean supports(Class requestContextClass, Class responseClass, Class serverTypeClass) {
        return LoadBalancerLifecycle.super.supports(requestContextClass, responseClass, serverTypeClass);
    }

    @Override
    public void onStart(Request<Object> request) {
    }

    @Override
    public void onStartRequest(Request<Object> request, Response<ServiceInstance> lbResponse) {
    }

    @Override
    public void onComplete(CompletionContext<Object, ServiceInstance, Object> completionContext) {
        Response<ServiceInstance> loadBalancerResponse = completionContext.getLoadBalancerResponse();
        ServiceInstance serviceInstance;
        if (loadBalancerResponse != null && (serviceInstance = loadBalancerResponse.getServer()) != null) {
            Throwable throwable = completionContext.getThrowable();
            if (throwable != null) {
                ServiceInstanceWeightSupport.downWeight(serviceInstance);
            } else {
                ServiceInstanceWeightSupport.restoreWeight(serviceInstance);
            }
        }
    }
}
