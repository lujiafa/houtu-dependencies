package com.houtu.springcloud.loadbalancer.support;

import com.houtu.springcloud.loadbalancer.type.ServiceInstanceType;
import org.springframework.cloud.client.ServiceInstance;

import java.math.BigDecimal;
import java.util.Map;

public final class ServiceInstanceWeightSupport {

    public static int downWeight(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata == null) {
            return SpringCloudWeightFunction.build().apply(serviceInstance);
        }
        ServiceInstanceType serviceInstanceType = ServiceInstanceType.parse(serviceInstance);
        String weightStr = metadata.get(serviceInstanceType.getWeightName());
        BigDecimal weightBigDecimal = BigDecimal.valueOf(SpringCloudWeightFunction.DEFAULT_WEIGHT);
        if (weightStr != null && !weightStr.isEmpty()) {
            String originalWeightStr = metadata.get("origin." + serviceInstanceType.getWeightName());
            if (originalWeightStr == null) {
                metadata.put("origin." + serviceInstanceType.getWeightName(), weightStr);
            }
            weightBigDecimal = new BigDecimal(weightStr);
        }
        weightBigDecimal = weightBigDecimal.multiply(BigDecimal.valueOf(0.8));
        int weight = weightBigDecimal.intValue();
        if (weight <= 0) {
            weight = SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        metadata.put(serviceInstanceType.getWeightName(), String.valueOf(weight));
        return weight;
    }


    public static int restoreWeight(ServiceInstance serviceInstance) {
        String originalWeightStr;
        ServiceInstanceType serviceInstanceType;
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata == null || (originalWeightStr = metadata.get("origin." + (serviceInstanceType = ServiceInstanceType.parse(serviceInstance)).getWeightName())) == null) {
            return SpringCloudWeightFunction.build().apply(serviceInstance);
        }
        int originalWeight = new BigDecimal(originalWeightStr).intValue();
        if (originalWeight <= 0) {
            originalWeight = SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        String weightStr = metadata.get(serviceInstanceType.getWeightName());
        BigDecimal weightBigDecimal = BigDecimal.valueOf(SpringCloudWeightFunction.DEFAULT_WEIGHT);
        if (weightStr != null && !weightStr.isEmpty()) {
            weightBigDecimal = new BigDecimal(weightStr);
        }
        weightBigDecimal = weightBigDecimal.multiply(BigDecimal.valueOf(1.1));
        int weight = weightBigDecimal.intValue();
        if (weight > originalWeight) {
            weight = originalWeight;
        }
        metadata.put(serviceInstanceType.getWeightName(), String.valueOf(weight));
        return weight;
    }
}
