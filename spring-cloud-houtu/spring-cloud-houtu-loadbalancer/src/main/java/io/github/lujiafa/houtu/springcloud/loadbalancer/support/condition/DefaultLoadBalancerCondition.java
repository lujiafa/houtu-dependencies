package io.github.lujiafa.houtu.springcloud.loadbalancer.support.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;

import java.util.List;

public class DefaultLoadBalancerCondition extends NoneNestedConditions {

    public DefaultLoadBalancerCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @Override
    protected ConditionOutcome getFinalMatchOutcome(MemberMatchOutcomes memberOutcomes) {
        List<ConditionOutcome> conditionOutcomes = memberOutcomes.getMatches();
        if (conditionOutcomes != null && conditionOutcomes.size() > 0) {
            if (conditionOutcomes.stream().allMatch(ConditionOutcome::isMatch)) {
                return ConditionOutcome.noMatch("DefaultLoadBalancerCondition no match");
            }
        }
        return ConditionOutcome.match();
    }

    @ConditionalOnClass({com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled.class})
    @com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled
    static class NacosDiscoveryCondition {
    }


}
