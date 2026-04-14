package io.github.lujiafa.houtu.springcloud.loadbalancer.support;

import io.github.lujiafa.houtu.springcloud.loadbalancer.type.ServiceInstanceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.WeightFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class SpringCloudWeightFunction implements WeightFunction {

    private static final Logger logger = LoggerFactory.getLogger(SpringCloudWeightFunction.class);

    static final SpringCloudWeightFunction INSTANCE = new SpringCloudWeightFunction();

    static final int DEFAULT_WEIGHT = 1;

    public static WeightFunction build() {
        return INSTANCE;
    }

    @Override
    public int apply(ServiceInstance serviceInstance) {
        return getWeight(serviceInstance, ServiceInstanceType.parse(serviceInstance).getWeightName());
    }

    int getWeight(ServiceInstance serviceInstance, String weightName) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null) {
            String weightValue = metadata.get(weightName);
            if (weightValue != null && !weightValue.isEmpty()) {
                try {
                    return new BigDecimal(weightValue).setScale(0, RoundingMode.CEILING).intValue();
                } catch (Exception e) {
                    logger.error("Failed to parse weight value: {}", weightValue, e);
                }
            }
        }
        return DEFAULT_WEIGHT;
    }

}
