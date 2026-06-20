package io.github.lujiafa.houtu.util.autoconfigure;

import io.github.lujiafa.houtu.util.prop.HttpClient5Properties;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.client.ClientHttpRequestFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@AutoConfiguration(after = HttpClientAutoConfiguration.class)
@EnableConfigurationProperties(HttpClient5Properties.class)
@ConditionalOnClass(CloseableHttpClient.class)
public class HttpClient5AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpComponentsClientHttpRequestFactoryBuilderCustomizer httpComponentsClientHttpRequestFactoryBuilderCustomizer(HttpClient5Properties httpClientProperties) {
        return new HttpComponentsClientHttpRequestFactoryBuilderCustomizer(httpClientProperties);
    }

    public static class HttpComponentsClientHttpRequestFactoryBuilderCustomizer implements ClientHttpRequestFactoryBuilderCustomizer<HttpComponentsClientHttpRequestFactoryBuilder> {

        private final HttpClient5Properties httpClientProperties;

        public HttpComponentsClientHttpRequestFactoryBuilderCustomizer(HttpClient5Properties httpClientProperties) {
            this.httpClientProperties = httpClientProperties;
        }

        @Override
        public HttpComponentsClientHttpRequestFactoryBuilder customize(HttpComponentsClientHttpRequestFactoryBuilder builder) {
            HttpClient5Properties.PoolProperties pool = httpClientProperties.getPool();
            HttpClient5Properties.RequestProperties request = httpClientProperties.getRequest();
            HttpClient5Properties.ProxyProperties proxy = httpClientProperties.getProxy();

            // 每个 withXxx 都返回新的不可变 builder，且 customizer 在 Spring Boot 默认设置之后执行（会覆盖默认值）
            return builder
                    .withConnectionManagerCustomizer(connectionManagerBuilder -> {
                        connectionManagerBuilder.setMaxConnTotal(pool.getMaxTotal());
                        connectionManagerBuilder.setMaxConnPerRoute(pool.getMaxPerRoute());
                        if (pool.getPoolReusePolicy() != null) {
                            connectionManagerBuilder.setConnPoolPolicy(pool.getPoolReusePolicy());
                        }
                        if (pool.getPoolConcurrencyPolicy() != null) {
                            connectionManagerBuilder.setPoolConcurrencyPolicy(pool.getPoolConcurrencyPolicy());
                        }
                        if (Boolean.TRUE.equals(pool.getDisableSslValidation())) {
                            connectionManagerBuilder.setTlsSocketStrategy(disableTlsSocketStrategy());
                        }
                    })
                    .withConnectionConfigCustomizer(connectionConfigBuilder -> {
                        if (request.getConnectTimeout() != null) {
                            connectionConfigBuilder.setConnectTimeout(Timeout.ofMilliseconds(request.getConnectTimeout().toMillis()));
                        }
                        if (request.getReadTimeout() != null) {
                            connectionConfigBuilder.setSocketTimeout(Timeout.ofMilliseconds(request.getReadTimeout().toMillis()));
                        }
                        if (pool.getTimeToLive() != null) {
                            connectionConfigBuilder.setTimeToLive(TimeValue.ofMilliseconds(pool.getTimeToLive().toMillis()));
                        }
                        if (pool.getValidateAfterInactivity() != null) {
                            connectionConfigBuilder.setValidateAfterInactivity(TimeValue.ofMilliseconds(pool.getValidateAfterInactivity().toMillis()));
                        }
                    })
                    .withDefaultRequestConfigCustomizer(requestConfigBuilder -> {
                        if (request.getRedirectsEnabled() != null) {
                            requestConfigBuilder.setRedirectsEnabled(request.getRedirectsEnabled());
                        }
                        if (request.getConnectionKeepAlive() != null) {
                            requestConfigBuilder.setConnectionKeepAlive(TimeValue.ofMilliseconds(request.getConnectionKeepAlive().toMillis()));
                        }
                    })
                    .withHttpClientCustomizer(httpClientBuilder -> {
                        if (StringUtils.hasText(proxy.getHostname()) && proxy.getPort() > 0) {
                            httpClientBuilder.setProxy(new HttpHost(proxy.getHostname(), proxy.getPort()));
                        }
                        if (StringUtils.hasText(request.getUserAgent())) {
                            httpClientBuilder.setUserAgent(request.getUserAgent());
                        }
                        if (pool.isEvictExpiredConnections()) {
                            httpClientBuilder.evictExpiredConnections();
                        }
                        if (pool.getEvictIdleTime() != null) {
                            httpClientBuilder.evictIdleConnections(TimeValue.ofMilliseconds(pool.getEvictIdleTime().toMillis()));
                        }
                        if (httpClientProperties.isDisableCookieManagement()) {
                            httpClientBuilder.disableCookieManagement();
                        }
                    });
        }

        /**
         * 构建信任全部证书并跳过主机名校验的 TLS 套接字策略（仅用于测试/内网/对接旧服务等场景）。
         * <p>仅当 {@code pool.disable-ssl-validation=true} 时调用；启用 TLS 1.0~1.3 以兼容老旧服务端
         * （TLS 1.0/1.1 可能受 JDK 的 {@code jdk.tls.disabledAlgorithms} 限制，需在 JVM 层放开）。
         */
        private TlsSocketStrategy disableTlsSocketStrategy() {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }}, new SecureRandom());
                return ClientTlsStrategyBuilder.create()
                        .setSslContext(sslContext)
                        .setTlsVersions(TLS.V_1_0, TLS.V_1_1, TLS.V_1_2, TLS.V_1_3)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .buildClassic();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IllegalStateException("Failed to init trust-all SSL context", e);
            }
        }
    }
}
