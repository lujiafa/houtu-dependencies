package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint;

import io.github.lujiafa.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.HintRequestContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HintBasedServiceInstanceListSupplier extends org.springframework.cloud.loadbalancer.core.HintBasedServiceInstanceListSupplier {

    private final LoadBalancerProperties properties;

    public HintBasedServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, ReactiveLoadBalancer.Factory<ServiceInstance> factory) {
        super(delegate, factory);
        this.properties = factory.getProperties(this.getServiceId());
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return super.get().map((instances) -> {
            return this.filteredByHint(instances, this.getHint(null));
        });
    }

    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        return this.delegate.get(request).map((instances) -> {
            return this.filteredByHint(instances, this.getHint(request.getContext()));
        });
    }

    protected String getHint(Object requestContext) {
        String hint;
        if (requestContext instanceof RequestDataContext) {
            RequestDataContext context = (RequestDataContext) requestContext;
            if (context.getClientRequest() != null) {
                HttpHeaders headers = context.getClientRequest().getHeaders();
                if (headers != null && StringUtils.hasText(hint = headers.getFirst(this.properties.getHintHeaderName()))) {
                    return hint;
                }
            }
        }

        HintContext.InnerHintData innerHintData = HintContext.get();
        if (StringUtils.hasText(hint = innerHintData.getHint())) {
            return hint;
        }

        if (requestContext instanceof HintRequestContext
                && StringUtils.hasText(hint = ((HintRequestContext) requestContext).getHint())) {
            return hint;
        }

        // 与注释代码等价
        hint = innerHintData.getXHint();
        /**
        RequestDataContext context = (RequestDataContext)requestContext;
        if (context.getClientRequest() != null) {
            HttpHeaders headers = context.getClientRequest().getHeaders();
            if (headers != null) {
                hint = headers.getFirst(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME);
            }
        }
        **/
        return hint;
    }

    protected List<ServiceInstance> filteredByHint(List<ServiceInstance> instances, String hint) {
        Iterator<ServiceInstance> var4 = instances.iterator();
        List<ServiceInstance> defaultInstances = new ArrayList<>();
        if (!StringUtils.hasText(hint)) {
            while(var4.hasNext()) {
                ServiceInstance serviceInstance = var4.next();
                Map<String, String> metadata = serviceInstance.getMetadata();
                String metaHint = metadata != null ? metadata.get(LoadBalancerConstant.METADATA_HINT_NAME) : null;
                if (!StringUtils.hasText(metaHint)) {
                    defaultInstances.add(serviceInstance);
                }
            }
            if (!defaultInstances.isEmpty()) {
                return defaultInstances;
            }
            return instances;
        }

        List<ServiceInstance> filteredInstances = new ArrayList<>();
        while(var4.hasNext()) {
            ServiceInstance serviceInstance = var4.next();
            Map<String, String> metadata = serviceInstance.getMetadata();
            String metaHint = metadata != null ? metadata.get(LoadBalancerConstant.METADATA_HINT_NAME) : null;
            if (!StringUtils.hasText(metaHint)) {
                defaultInstances.add(serviceInstance);
                continue;
            }
            if (metaHint.equals(hint)) {
                filteredInstances.add(serviceInstance);
            }
        }
        if (!filteredInstances.isEmpty()) {
            return filteredInstances;
        }
        if (!defaultInstances.isEmpty()) {
            return defaultInstances;
        }
        return instances;
    }
}
