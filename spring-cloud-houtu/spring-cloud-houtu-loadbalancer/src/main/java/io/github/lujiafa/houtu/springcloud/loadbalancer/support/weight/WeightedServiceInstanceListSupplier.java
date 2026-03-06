package io.github.lujiafa.houtu.springcloud.loadbalancer.support.weight;

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

    public WeightedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate,
                                               ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerClientFactory) {
        this(delegate, WeightedServiceInstanceListSupplier::metadataWeightFunction, loadBalancerClientFactory);
    }

    public WeightedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, WeightFunction weightFunction,
                                               ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerClientFactory) {
        super(delegate);
        this.weightFunction = weightFunction;
        callGetWithRequestOnDelegates = loadBalancerClientFactory.getProperties(getServiceId())
                .isCallGetWithRequestOnDelegates();
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return delegate.get().map(this::expandByWeight);
    }

    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        if (callGetWithRequestOnDelegates) {
            return delegate.get(request).map(this::expandByWeight);
        }
        return get();
    }

    private List<ServiceInstance> expandByWeight(List<ServiceInstance> instances) {
        if (instances.size() == 0) {
            return instances;
        }

        int[] weights = instances.stream().mapToInt(instance -> {
            try {
                int weight = weightFunction.apply(instance);
                if (weight <= 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "The weight of the instance %s should be a positive integer, but it got %d, using %d as default",
                                instance.getInstanceId(), weight, DEFAULT_WEIGHT));
                    }
                    return DEFAULT_WEIGHT;
                }
                return weight;
            }
            catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(
                            "Exception occurred during apply weight function to instance %s, using %d as default",
                            instance.getInstanceId(), DEFAULT_WEIGHT), e);
                }
                return DEFAULT_WEIGHT;
            }
        }).toArray();

        return new LazyWeightedServiceInstanceList(instances, weights);
    }

    static int metadataWeightFunction(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null) {
            String weightValue = metadata.get(METADATA_WEIGHT_KEY);
            if (weightValue != null) {
                return Integer.parseInt(weightValue);
            }
        }
        // using default weight when metadata is missing or
        // weight is not specified
        return DEFAULT_WEIGHT;
    }

}
