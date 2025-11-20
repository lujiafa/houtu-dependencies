package com.houtu.springcloud.loadbalancer.support;

import com.houtu.springcloud.loadbalancer.support.weight.WeightedServiceInstanceListSupplier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SpringCloudWeightedDelegateCreator implements ServiceInstanceListSupplierBuilder.DelegateCreator {

    static final SpringCloudWeightedDelegateCreator INSTANCE = new SpringCloudWeightedDelegateCreator();

    public static SpringCloudWeightedDelegateCreator build() {
        return INSTANCE;
    }

    @Override
    public ServiceInstanceListSupplier apply(ConfigurableApplicationContext context, ServiceInstanceListSupplier serviceInstanceListSupplier) {
        ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerClientFactory = context.getBean(LoadBalancerClientFactory.class);
        return new WeightedServiceInstanceListSupplier(serviceInstanceListSupplier, loadBalancerClientFactory);
    }

    @Override
    public <V> BiFunction<ConfigurableApplicationContext, ServiceInstanceListSupplier, V> andThen(Function<? super ServiceInstanceListSupplier, ? extends V> after) {
        return ServiceInstanceListSupplierBuilder.DelegateCreator.super.andThen(after);
    }
}
