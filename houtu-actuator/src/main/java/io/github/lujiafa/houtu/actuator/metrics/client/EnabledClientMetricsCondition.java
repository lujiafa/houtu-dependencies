package io.github.lujiafa.houtu.actuator.metrics.client;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class EnabledClientMetricsCondition extends AnyNestedCondition {

    public EnabledClientMetricsCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "management.metrics.distribution.percentiles", name = "http.client.requests")
    static class EnabledDistributionClientMetricsCondition {}

    @ConditionalOnProperty(prefix = "management.metrics.web.client.request.autotime", name = "percentiles")
    static class EnabledWebClientMetricsCondition {}


}
