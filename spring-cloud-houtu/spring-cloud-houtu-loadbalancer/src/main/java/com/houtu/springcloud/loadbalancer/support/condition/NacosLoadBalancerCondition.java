package com.houtu.springcloud.loadbalancer.support.condition;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

public class NacosLoadBalancerCondition extends AllNestedConditions {

    public NacosLoadBalancerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass({com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled.class})
    @com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled
    static class NacosDiscoveryCondition {
    }


}
