package io.github.lujiafa.houtu.springcloud.loadbalancer.support;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 参考：
 * org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer
 * com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer
 *
 * @author jonlu
 * @date 2020/7/23
 */
public class SpringCloudLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private static final Logger log = LoggerFactory.getLogger(SpringCloudLoadBalancer.class);

    static final String DEFAULT_METADATA_CLUSTER_NAME = "cluster";

    private final String serviceId;
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String clusterName;
    private final AtomicLong position;
    private Function<ServiceInstance, String> clusterNameFunction;

    public SpringCloudLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, String clusterName) {
        this(serviceInstanceListSupplierProvider, serviceId, clusterName, null);
    }

    public SpringCloudLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, String clusterName, Function<ServiceInstance, String> clusterNameFunction) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.clusterName = clusterName;
        this.clusterNameFunction = clusterNameFunction == null ? serviceInstance -> {
            Map<String, String> metadata = serviceInstance.getMetadata();
            return metadata != null ? metadata.get(DEFAULT_METADATA_CLUSTER_NAME) : null;
        } : clusterNameFunction;
        this.position = new AtomicLong((new Random()).nextInt(1000));
    }


    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map((serviceInstances) -> this.processInstanceResponse(supplier, serviceInstances));
    }

    protected Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance((ServiceInstance) serviceInstanceResponse.getServer());
        }

        return serviceInstanceResponse;
    }

    protected Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: {}", this.serviceId);
            return new EmptyResponse();
        }
        try {
            List<ServiceInstance> instancesToChoose = serviceInstances;
            if (StringUtils.isNotBlank(this.clusterName)) {
                List<ServiceInstance> sameClusterInstances = serviceInstances.stream().filter((serviceInstance) -> {
                    return StringUtils.equals(clusterNameFunction.apply(serviceInstance), clusterName);
                }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                    instancesToChoose = sameClusterInstances;
                }
            }

            if (instancesToChoose.size() == 1) {
                return new DefaultResponse(instancesToChoose.get(0));
            } else {
                long pos = this.position.incrementAndGet() & Long.MAX_VALUE;
                ServiceInstance instance = instancesToChoose.get((int) (pos % instancesToChoose.size()));
                return new DefaultResponse(instance);
            }
        } catch (Exception e) {
            log.warn("io.github.lujiafa.support.loadbalancer.springcloud.houtu.SpringCloudLoadBalancer error", e);
            return new EmptyResponse();
        }
    }

}
