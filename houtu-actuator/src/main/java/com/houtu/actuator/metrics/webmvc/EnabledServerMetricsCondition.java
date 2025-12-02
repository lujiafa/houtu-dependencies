package com.houtu.actuator.metrics.webmvc;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class EnabledServerMetricsCondition extends AnyNestedCondition {

    public EnabledServerMetricsCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "management.metrics.distribution.percentiles", name = "http.server.requests")
    static class EnabledDistributionServerMetricsCondition {}

    @ConditionalOnProperty(prefix = "management.metrics.web.server.request.autotime", name = "percentiles")
    static class EnabledWebServerMetricsCondition {}


}
