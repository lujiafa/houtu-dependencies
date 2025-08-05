package com.houtu.springcloud.feign.provider.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = FeignProviderProperties.PREFIX)
public class FeignProviderProperties {
    final static String PREFIX = "spring.cloud.feign.provider";

    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
