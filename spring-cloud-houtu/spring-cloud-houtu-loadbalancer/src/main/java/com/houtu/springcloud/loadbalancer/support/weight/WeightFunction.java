package com.houtu.springcloud.loadbalancer.support.weight;

import org.springframework.cloud.client.ServiceInstance;

@FunctionalInterface
public interface WeightFunction {
    int apply(ServiceInstance instance);
}
