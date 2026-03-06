package io.github.lujiafa.houtu.util.prop;

import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = HttpClientProperties.PREFIX)
public class HttpClientProperties {

    final static String PREFIX = "houtu.util.httpclient";

    private PoolProperties pool = new PoolProperties();
    private RequestProperties request = new RequestProperties();
    private ProxyProperties proxy = new ProxyProperties();

    public PoolProperties getPool() {
        return pool;
    }

    public void setPool(PoolProperties pool) {
        this.pool = pool;
    }

    public RequestProperties getRequest() {
        return request;
    }

    public void setRequest(RequestProperties request) {
        this.request = request;
    }

    public ProxyProperties getProxy() {
        return proxy;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }

    public static class PoolProperties {
        // 设置最大连接数
        private int maxTotal = 200;
        // 设置每个路由的默认最大连接
        private int maxPerRoute = 50;

        private Boolean disableSslValidation;
        private PoolReusePolicy poolReusePolicy;
        private PoolConcurrencyPolicy poolConcurrencyPolicy;

        public int getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
        }

        public int getMaxPerRoute() {
            return maxPerRoute;
        }

        public void setMaxPerRoute(int maxPerRoute) {
            this.maxPerRoute = maxPerRoute;
        }

        public Boolean getDisableSslValidation() {
            return disableSslValidation;
        }

        public void setDisableSslValidation(Boolean disableSslValidation) {
            this.disableSslValidation = disableSslValidation;
        }

        public PoolReusePolicy getPoolReusePolicy() {
            return poolReusePolicy;
        }

        public void setPoolReusePolicy(PoolReusePolicy poolReusePolicy) {
            this.poolReusePolicy = poolReusePolicy;
        }

        public PoolConcurrencyPolicy getPoolConcurrencyPolicy() {
            return poolConcurrencyPolicy;
        }

        public void setPoolConcurrencyPolicy(PoolConcurrencyPolicy poolConcurrencyPolicy) {
            this.poolConcurrencyPolicy = poolConcurrencyPolicy;
        }
    }

    public static class RequestProperties {
        private Duration connectTimeout = Duration.ofSeconds(5);
        private Duration responseTimeout = Duration.ofSeconds(15);

        private Duration connectionKeepAlive;

        private String userAgent;

        private boolean redirectsEnabled;

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getResponseTimeout() {
            return responseTimeout;
        }

        public void setResponseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
        }

        public Duration getConnectionKeepAlive() {
            return connectionKeepAlive;
        }

        public void setConnectionKeepAlive(Duration connectionKeepAlive) {
            this.connectionKeepAlive = connectionKeepAlive;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public boolean isRedirectsEnabled() {
            return redirectsEnabled;
        }

        public void setRedirectsEnabled(boolean redirectsEnabled) {
            this.redirectsEnabled = redirectsEnabled;
        }
    }

    public static class ProxyProperties {
        private String hostname;
        private int port;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
