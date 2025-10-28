package com.houtu.springcloud.loadbalancer.support.weight;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

public class WeightedServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {
    private static final Log LOG = LogFactory.getLog(WeightedServiceInstanceListSupplier.class);
    static final String METADATA_WEIGHT_KEY = "weight";
    static final int DEFAULT_WEIGHT = 1;
    private final WeightFunction weightFunction;
    private boolean callGetWithRequestOnDelegates;

    public WeightedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
        this(delegate, WeightedServiceInstanceListSupplier::metadataWeightFunction);
    }

    public WeightedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, WeightFunction weightFunction) {
        super(delegate);
        this.weightFunction = weightFunction;
    }

    public WeightedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerClientFactory) {
        this(delegate, WeightedServiceInstanceListSupplier::metadataWeightFunction, loadBalancerClientFactory);
    }

    public WeightedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, WeightFunction weightFunction, ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerClientFactory) {
        super(delegate);
        this.weightFunction = weightFunction;
        this.callGetWithRequestOnDelegates = loadBalancerClientFactory.getProperties(this.getServiceId()).isCallGetWithRequestOnDelegates();
    }

    public Flux<List<ServiceInstance>> get() {
        return this.delegate.get().map(this::expandByWeight);
    }

    public Flux<List<ServiceInstance>> get(Request request) {
        return this.callGetWithRequestOnDelegates ? this.delegate.get(request).map(this::expandByWeight) : this.get();
    }

    private List<ServiceInstance> expandByWeight(List<ServiceInstance> instances) {
        if (instances.size() == 0) {
            return instances;
        } else {
            int[] weights = instances.stream().mapToInt((instance) -> {
                try {
                    int weight = this.weightFunction.apply(instance);
                    if (weight <= 0) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("The weight of the instance %s should be a positive integer, but it got %d, using %d as default", instance.getInstanceId(), weight, 1));
                        }

                        return 1;
                    } else {
                        return weight;
                    }
                } catch (Exception var3) {
                    Exception e = var3;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Exception occurred during apply weight function to instance %s, using %d as default", instance.getInstanceId(), 1), e);
                    }

                    return 1;
                }
            }).toArray();
            return new LazyWeightedServiceInstanceList(instances, weights);
        }
    }

    static int metadataWeightFunction(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null) {
            String weightValue = (String)metadata.get("weight");
            if (weightValue != null) {
                return Integer.parseInt(weightValue);
            }
        }

        return 1;
    }
}
