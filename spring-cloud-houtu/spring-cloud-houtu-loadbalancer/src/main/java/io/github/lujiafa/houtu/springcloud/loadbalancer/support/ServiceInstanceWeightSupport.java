package io.github.lujiafa.houtu.springcloud.loadbalancer.support;

import io.github.lujiafa.houtu.springcloud.loadbalancer.type.ServiceInstanceType;
import org.slf4j.Logger;
import org.springframework.cloud.client.ServiceInstance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class ServiceInstanceWeightSupport {

    static final String ORIGIN_PREFIX = "origin.";
    private static final BigDecimal DOWNGRADE_FACTOR = BigDecimal.valueOf(0.8);
    private static final BigDecimal UPGRADE_FACTOR = BigDecimal.valueOf(1.1);

    static Logger logger = org.slf4j.LoggerFactory.getLogger(ServiceInstanceWeightSupport.class);

    /**
     * 降级服务实例权重
     * @param serviceInstance
     * @return int
     */
    public static int downWeight(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata == null) {
            return SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        ServiceInstanceType serviceInstanceType = ServiceInstanceType.parse(serviceInstance);
        String weightStr = metadata.get(serviceInstanceType.getWeightName());
        BigDecimal weightBigDecimal;
        int weight;
        if (weightStr == null || weightStr.isEmpty()) {
            return SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        weightBigDecimal = new BigDecimal(weightStr);
        if (weightBigDecimal.compareTo(BigDecimal.ONE) <= 0) {
            return SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        String originalWeightKey = ORIGIN_PREFIX + serviceInstanceType.getWeightName();
        String originalWeightStr = metadata.get(originalWeightKey);
        if (originalWeightStr == null) {
            metadata.put(originalWeightKey, weightStr);
        }
        weightBigDecimal = weightBigDecimal.multiply(DOWNGRADE_FACTOR);
        weight = weightBigDecimal.setScale(0, RoundingMode.FLOOR).intValue();
        if (weight <= 0) {
            weight = SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        metadata.put(serviceInstanceType.getWeightName(), String.valueOf(weight));
        logger.info("降级服务实例权重({}={}:{}, instanceId={})：{} -> {}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort(), serviceInstance.getInstanceId(), weightStr, weight);
        return weight;
    }


    /**
     * 恢复服务实例权重
     * @param serviceInstance
     * @return int
     */
    public static int restoreWeight(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata == null) {
            return SpringCloudWeightFunction.DEFAULT_WEIGHT;
        }
        ServiceInstanceType serviceInstanceType = ServiceInstanceType.parse(serviceInstance);
        String originalWeightKey = ORIGIN_PREFIX + serviceInstanceType.getWeightName();
        String originalWeightStr = metadata.get(originalWeightKey);
        if (originalWeightStr == null) {
            return SpringCloudWeightFunction.build().apply(serviceInstance);
        }
        int originalWeight =  (originalWeight = new BigDecimal(originalWeightStr).setScale(0, RoundingMode.CEILING).intValue()) <= 0 ? SpringCloudWeightFunction.DEFAULT_WEIGHT : originalWeight;
        String weightStr = metadata.get(serviceInstanceType.getWeightName());
        BigDecimal weightBigDecimal = weightStr != null && !weightStr.isEmpty() ? new BigDecimal(weightStr) : BigDecimal.valueOf(SpringCloudWeightFunction.DEFAULT_WEIGHT);
        int weight = weightBigDecimal.multiply(UPGRADE_FACTOR).setScale(0, RoundingMode.CEILING).intValue();
        if (weight > originalWeight) {
            weight = originalWeight;
            metadata.remove(originalWeightKey);
        }
        metadata.put(serviceInstanceType.getWeightName(), String.valueOf(weight));
        logger.info("恢复服务实例权重({}={}:{}, instanceId={})：{} -> {}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort(), serviceInstance.getInstanceId(), weightStr, weight);
        return weight;
    }
}
