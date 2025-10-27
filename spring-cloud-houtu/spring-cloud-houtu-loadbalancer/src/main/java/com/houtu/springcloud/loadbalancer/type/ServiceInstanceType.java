package com.houtu.springcloud.loadbalancer.type;

//import com.houtu.springcloud.loadbalancer.support.SpringCloudWeightFunction;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.ClassUtils;

public enum ServiceInstanceType {

    NACOS("nacos.cluster", "nacos.weight"),
    DEFAULT("cluster", "weight");

//    static final Class<?> NACOS_CLASS = ClassUtils.isPresent("com.alibaba.cloud.nacos.NacosServiceInstance", SpringCloudWeightFunction.class.getClassLoader()) ? ClassUtils.resolveClassName("com.alibaba.cloud.nacos.NacosServiceInstance", SpringCloudWeightFunction.class.getClassLoader()) : null;
    static final Class<?> NACOS_CLASS = null;

    private String clusterName;
    private String weightName;

    ServiceInstanceType(String clusterName, String weightName) {
        this.clusterName = clusterName;
        this.weightName = weightName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getWeightName() {
        return weightName;
    }

    public static ServiceInstanceType parse(ServiceInstance serviceInstance) {
        if (NACOS_CLASS != null && NACOS_CLASS.isInstance(serviceInstance)) {
            return NACOS;
        }
        return DEFAULT;
    }
}