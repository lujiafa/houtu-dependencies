package com.houtu.springcloud.loadbalancer.support.weight;

import org.springframework.cloud.client.ServiceInstance;

@FunctionalInterface
public interface WeightFunction {

    /**
     * Applies this function to the given service instance.
     * @param instance the service instance
     * @return the weight of service instance
     */
    int apply(ServiceInstance instance);

}
