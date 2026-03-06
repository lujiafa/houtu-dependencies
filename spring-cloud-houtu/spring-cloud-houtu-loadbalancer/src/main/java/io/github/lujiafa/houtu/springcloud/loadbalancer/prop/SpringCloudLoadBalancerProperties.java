package io.github.lujiafa.houtu.springcloud.loadbalancer.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SpringCloudLoadBalancerProperties.PREFIX)
public class SpringCloudLoadBalancerProperties {
    public final static String PREFIX = "spring.cloud.loadbalancer";

    // 是否启用权重（默认启用）
    private boolean weight = true;
    // 是否启用hint（默认启用）
    private boolean hint = true;
    // SpringCloudGateway场景中，中是否去除请求头中"X-Hint"，防止干扰链路hint
    private boolean disableGatewayRequestHint = false;

    public boolean isWeight() {
        return weight;
    }

    public void setWeight(boolean weight) {
        this.weight = weight;
    }

    public boolean isHint() {
        return hint;
    }

    public void setHint(boolean hint) {
        this.hint = hint;
    }

    public boolean isDisableGatewayRequestHint() {
        return disableGatewayRequestHint;
    }

    public void setDisableGatewayRequestHint(boolean disableGatewayRequestHint) {
        this.disableGatewayRequestHint = disableGatewayRequestHint;
    }
}
