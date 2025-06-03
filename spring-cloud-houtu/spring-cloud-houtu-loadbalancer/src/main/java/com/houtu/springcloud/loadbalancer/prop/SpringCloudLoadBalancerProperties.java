package com.houtu.springcloud.loadbalancer.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SpringCloudLoadBalancerProperties.PREFIX)
public class SpringCloudLoadBalancerProperties {
    final static String PREFIX = "spring.cloud.loadbalancer";

    private SpringCloudLoadBalancerHintProperties hint = new SpringCloudLoadBalancerHintProperties();

    public SpringCloudLoadBalancerHintProperties getHint() {
        return hint;
    }

    public void setHint(SpringCloudLoadBalancerHintProperties hint) {
        this.hint = hint;
    }

    public static class SpringCloudLoadBalancerHintProperties {
        // 默认开启
        private boolean enable = true;
        // SpringCloudGateway场景中，中是否去除请求头中"X-SC-LB-Hint"，防止干扰链路hint
        private boolean enableGatewayRequestHeader = false;

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean isEnable() {
            return enable;
        }

        public boolean isEnableGatewayRequestHeader() {
            return enableGatewayRequestHeader;
        }

        public void setEnableGatewayRequestHeader(boolean enableGatewayRequestHeader) {
            this.enableGatewayRequestHeader = enableGatewayRequestHeader;
        }
    }

}
