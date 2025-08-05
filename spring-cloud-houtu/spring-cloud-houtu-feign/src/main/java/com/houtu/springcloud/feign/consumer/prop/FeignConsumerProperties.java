package com.houtu.springcloud.feign.consumer.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = FeignConsumerProperties.PREFIX)
public class FeignConsumerProperties {
    final static String PREFIX = "spring.cloud.feign.consumer";

    /**
     * 默认Feign请求密钥
     */
    private String secret;
    /**
     * Feign针对自定义服务的请求密钥
     */
    private Map<String, String> secrets = new HashMap<>();

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Map<String, String> getSecrets() {
        return secrets;
    }

    public void setSecrets(Map<String, String> secrets) {
        this.secrets = secrets;
    }
}
