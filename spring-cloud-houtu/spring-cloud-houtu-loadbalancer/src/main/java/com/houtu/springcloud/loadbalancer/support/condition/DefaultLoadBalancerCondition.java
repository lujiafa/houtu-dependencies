package com.houtu.springcloud.loadbalancer.support.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;

public class DefaultLoadBalancerCondition extends NoneNestedConditions {

    public DefaultLoadBalancerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass({com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled.class})
    @com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled
    static class NacosDiscoveryCondition {
    }


}
