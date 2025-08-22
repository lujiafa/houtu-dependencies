package com.houtu.websecurity.session.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = SessionClientProperties.PREFIX)
public class SessionClientProperties {

    public static final String PREFIX = "houtu.web.session.client";

    private String serverUrl;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
