package io.github.lujiafa.houtu.util.prop;

import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = HttpClient5Properties.PREFIX)
public class HttpClient5Properties {

    final static String PREFIX = "houtu.client.httpclient";

    private PoolProperties pool = new PoolProperties();
    private RequestProperties request = new RequestProperties();
    private ProxyProperties proxy = new ProxyProperties();

    // 是否禁用 Cookie 管理（保留旧默认行为）
    private boolean disableCookieManagement = true;

    public boolean isDisableCookieManagement() {
        return disableCookieManagement;
    }

    public void setDisableCookieManagement(boolean disableCookieManagement) {
        this.disableCookieManagement = disableCookieManagement;
    }

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
        private int maxTotal = 2000;
        // 设置每个路由的默认最大连接
        private int maxPerRoute = 500;

        private Boolean disableSslValidation;
        private PoolReusePolicy poolReusePolicy;
        private PoolConcurrencyPolicy poolConcurrencyPolicy;

        // 连接存活上限，null 表示不限制
        private Duration timeToLive;
        // 空闲多久后在复用前重新校验连接，null 表示使用默认
        private Duration validateAfterInactivity;
        // 是否驱逐已过期连接（保留旧默认行为）
        private boolean evictExpiredConnections = true;
        // 驱逐空闲连接的阈值，null 表示关闭空闲驱逐
        private Duration evictIdleTime;

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

        public Duration getTimeToLive() {
            return timeToLive;
        }

        public void setTimeToLive(Duration timeToLive) {
            this.timeToLive = timeToLive;
        }

        public Duration getValidateAfterInactivity() {
            return validateAfterInactivity;
        }

        public void setValidateAfterInactivity(Duration validateAfterInactivity) {
            this.validateAfterInactivity = validateAfterInactivity;
        }

        public boolean isEvictExpiredConnections() {
            return evictExpiredConnections;
        }

        public void setEvictExpiredConnections(boolean evictExpiredConnections) {
            this.evictExpiredConnections = evictExpiredConnections;
        }

        public Duration getEvictIdleTime() {
            return evictIdleTime;
        }

        public void setEvictIdleTime(Duration evictIdleTime) {
            this.evictIdleTime = evictIdleTime;
        }
    }

    public static class RequestProperties {
        // 连接超时，null 表示不设置（让位 spring.http.client.connect-timeout）
        private Duration connectTimeout;
        // 读超时（SO_TIMEOUT），null 表示不设置（让位 spring.http.client.read-timeout）
        private Duration readTimeout;

        private Duration connectionKeepAlive;

        private String userAgent;

        // 是否跟随重定向，null 表示不设置（让位 spring.http.client.redirects）
        private Boolean redirectsEnabled;

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
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

        public Boolean getRedirectsEnabled() {
            return redirectsEnabled;
        }

        public void setRedirectsEnabled(Boolean redirectsEnabled) {
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
