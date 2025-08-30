package com.houtu.springcloud.loadbalancer.support;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.WeightFunction;
import org.springframework.util.ClassUtils;

import java.math.BigDecimal;
import java.util.Map;

public class SpringCloudWeightFunction implements WeightFunction {

    static final SpringCloudWeightFunction INSTANCE = new SpringCloudWeightFunction();

    static final int DEFAULT_WEIGHT = 1;
    static final String DEFAULT_WEIGHT_NAME = "weight";

    static final boolean NACOS_PRESENT = ClassUtils.isPresent("com.alibaba.cloud.nacos.NacosServiceInstance", SpringCloudWeightFunction.class.getClassLoader());
    static Class<?> NACOS_CLASS;


    public static WeightFunction build() {
        return INSTANCE;
    }

    @Override
    public int apply(ServiceInstance instance) {
        if (NACOS_PRESENT) {
            if (NACOS_CLASS == null) {
                NACOS_CLASS = ClassUtils.resolveClassName("com.alibaba.cloud.nacos.NacosServiceInstance", this.getClass().getClassLoader());
            }
            if (NACOS_CLASS.isInstance(instance)) {
                return getWeightFromNacos(instance);
            }
        }
        return getWeight(instance);
    }

    int getWeightFromNacos(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null) {
            String nacosWeightStr = metadata.get("nacos.weight");
            if (nacosWeightStr != null && !nacosWeightStr.isEmpty()) {
                return new BigDecimal(nacosWeightStr).intValue();
            }
        }
        return DEFAULT_WEIGHT;
    }

    int getWeight(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null) {
            String weightValue = (String) metadata.get(DEFAULT_WEIGHT_NAME);
            if (weightValue != null) {
                return Integer.parseInt(weightValue);
            }
        }
        return DEFAULT_WEIGHT;
    }

}
