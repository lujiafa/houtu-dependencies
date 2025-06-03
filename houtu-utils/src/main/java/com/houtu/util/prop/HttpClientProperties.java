package com.houtu.util.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
        private int maxTotal = 80;
        // 设置每个路由的默认最大连接
        private int maxPerRoute = 10;

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
    }

    public static class RequestProperties {
        // 连接超时时间（秒）
        private int connectTimeout = 5;
        // 响应超时时间（秒）
        private int responseTimeout = 15;

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getResponseTimeout() {
            return responseTimeout;
        }

        public void setResponseTimeout(int responseTimeout) {
            this.responseTimeout = responseTimeout;
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
