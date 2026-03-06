package io.github.lujiafa.houtu.websecurity.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SignProperties.PREFIX)
public class SignProperties {

    public static final String PREFIX = "houtu.web.sign";

    private String signKey;

    public String getSignKey() {
        return signKey;
    }

    public void setSignKey(String signKey) {
        this.signKey = signKey;
    }
}
