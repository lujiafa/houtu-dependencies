package com.houtu.springcloud.loadbalancer.support;

import com.houtu.springcloud.loadbalancer.support.hint.HintBasedServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplierBuilder;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SpringCloudDelegateCreator implements ServiceInstanceListSupplierBuilder.DelegateCreator {

    static final SpringCloudDelegateCreator INSTANCE = new SpringCloudDelegateCreator();

    public static SpringCloudDelegateCreator build() {
        return INSTANCE;
    }

    @Override
    public ServiceInstanceListSupplier apply(ConfigurableApplicationContext configurableApplicationContext, ServiceInstanceListSupplier serviceInstanceListSupplier) {
        LoadBalancerClientFactory factory = (LoadBalancerClientFactory) configurableApplicationContext.getBean(LoadBalancerClientFactory.class);
        return new HintBasedServiceInstanceListSupplier(serviceInstanceListSupplier, factory);
    }

    @Override
    public <V> BiFunction<ConfigurableApplicationContext, ServiceInstanceListSupplier, V> andThen(Function<? super ServiceInstanceListSupplier, ? extends V> after) {
        return ServiceInstanceListSupplierBuilder.DelegateCreator.super.andThen(after);
    }
}
