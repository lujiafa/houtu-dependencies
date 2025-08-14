package com.houtu.springcloud.loadbalancer.support;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class NacosLoadBalancer extends com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer {
    private static final Logger log = LoggerFactory.getLogger(com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer.class);
    private final String serviceId;
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    public NacosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, NacosDiscoveryProperties nacosDiscoveryProperties) {
        super(serviceInstanceListSupplierProvider, serviceId, nacosDiscoveryProperties);
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }


    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(this::getInstanceResponse);
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: {}", this.serviceId);
            return new EmptyResponse();
        } else {
            try {
                String clusterName = this.nacosDiscoveryProperties.getClusterName();
                List<ServiceInstance> instancesToChoose = serviceInstances;
                if (StringUtils.isNotBlank(clusterName)) {
                    List<ServiceInstance> sameClusterInstances = (List)serviceInstances.stream().filter((serviceInstance) -> {
                        String cluster = (String)serviceInstance.getMetadata().get("discovery.cluster");
                        return StringUtils.equals(cluster, clusterName);
                    }).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                        instancesToChoose = sameClusterInstances;
                    }
                } else {
                    log.warn("A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}", new Object[]{this.serviceId, clusterName, serviceInstances});
                }

                ServiceInstance instance = NacosBalancer.getHostByRandomWeight3(instancesToChoose);
                return new DefaultResponse(instance);
            } catch (Exception var5) {
                log.warn("NacosLoadBalancer error", var5);
                return null;
            }
        }
    }
}
