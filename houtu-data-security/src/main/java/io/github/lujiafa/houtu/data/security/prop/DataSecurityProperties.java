package io.github.lujiafa.houtu.data.security.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = DataSecurityProperties.PROPERTIES_PREFIX)
public class DataSecurityProperties {

    static final String PROPERTIES_PREFIX = "houtu.data.security";

    /** 密钥 */
    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
