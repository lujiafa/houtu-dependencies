package io.github.lujiafa.houtu.springcloud.loadbalancer.support.condition;

import io.github.lujiafa.houtu.springcloud.loadbalancer.prop.SpringCloudLoadBalancerProperties;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class EnabledHintCondition extends AllNestedConditions {

    public EnabledHintCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(name = SpringCloudLoadBalancerProperties.PREFIX + ".hint", havingValue = "true", matchIfMissing = true)
    static class EnabledHintConditionPropertiesCondition {
    }


}
