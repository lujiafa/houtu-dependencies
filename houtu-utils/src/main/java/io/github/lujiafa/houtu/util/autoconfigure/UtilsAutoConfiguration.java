package io.github.lujiafa.houtu.util.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lujiafa.houtu.util.common.JsonUtils;
import io.github.lujiafa.houtu.util.http.HttpClients;
import io.github.lujiafa.houtu.util.prop.HttpClientProperties;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@AutoConfiguration
@EnableConfigurationProperties(HttpClientProperties.class)
public class UtilsAutoConfiguration {

    @Primary
    @Bean(destroyMethod = "close")
    @ConditionalOnClass(HttpClient.class)
    @ConditionalOnMissingBean(value = CloseableHttpClient.class, name = "httpClient")
    public CloseableHttpClient httpClient(HttpClientProperties httpClientProperties,
                                          List<HttpRequestInterceptor> requestInterceptors,
                                          List<HttpResponseInterceptor> responseInterceptors,
                                          List<ExecChainHandler> execChainHandlers,
                                          ApplicationContext applicationContext) {
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(httpClientProperties.getPool().getMaxTotal())
                .setMaxConnPerRoute(httpClientProperties.getPool().getMaxPerRoute())
                .setSSLSocketFactory(httpsSSLConnectionSocketFactory(httpClientProperties.getPool().getDisableSslValidation()));
        if (httpClientProperties.getPool().getPoolReusePolicy() != null) {
            connectionManagerBuilder.setConnPoolPolicy(httpClientProperties.getPool().getPoolReusePolicy());
        }
        if (httpClientProperties.getPool().getPoolConcurrencyPolicy() != null) {
            connectionManagerBuilder.setPoolConcurrencyPolicy(httpClientProperties.getPool().getPoolConcurrencyPolicy());
        }

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(httpClientProperties.getRequest().getConnectTimeout().getSeconds()))
                .setResponseTimeout(Timeout.ofSeconds(httpClientProperties.getRequest().getResponseTimeout().getSeconds()))
                .setRedirectsEnabled(httpClientProperties.getRequest().isRedirectsEnabled());
        if (httpClientProperties.getRequest().getConnectionKeepAlive() != null) {
            requestConfigBuilder.setConnectionKeepAlive(TimeValue.ofSeconds(httpClientProperties.getRequest().getConnectionKeepAlive().getSeconds()));
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setConnectionManager(connectionManagerBuilder.build())
                .setDefaultRequestConfig(requestConfigBuilder.build())
                .evictExpiredConnections()
                .disableCookieManagement();
        if (StringUtils.hasLength(httpClientProperties.getProxy().getHostname())
                && httpClientProperties.getProxy().getPort() > 0) {
            httpClientBuilder.setProxy(new HttpHost(httpClientProperties.getProxy().getHostname(), httpClientProperties.getProxy().getPort()));
        }
        if (StringUtils.hasLength(httpClientProperties.getRequest().getUserAgent())) {
            httpClientBuilder.setUserAgent(httpClientProperties.getRequest().getUserAgent());
        }
        if (requestInterceptors != null && !requestInterceptors.isEmpty()) {
            requestInterceptors.stream().forEach(i -> httpClientBuilder.addRequestInterceptorLast(i));
        }
        if (responseInterceptors != null && !responseInterceptors.isEmpty()) {
            responseInterceptors.stream().forEach(i -> httpClientBuilder.addResponseInterceptorLast(i));
        }
        if (execChainHandlers != null && !execChainHandlers.isEmpty()) {
            Collections.reverse(execChainHandlers);
            Map<ExecChainHandler, String> execChainHandlerStringMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ExecChainHandler.class).entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
            AtomicInteger incr = new AtomicInteger(0);
            execChainHandlers.forEach(e -> {
                String beanName = execChainHandlerStringMap.get(e);
                if (!StringUtils.hasText(beanName)) {
                    beanName = String.format("%s-%d", Thread.currentThread().getName(), incr.getAndIncrement());
                }
                httpClientBuilder.addExecInterceptorFirst(beanName, e);
            });
        }
        return httpClientBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(HttpClient.class)
    @Scope(value = "singleton")
    public HttpClients httpClients(CloseableHttpClient httpClient) {
        return new HttpClients(httpClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @Scope(value = "singleton")
    public JsonUtils jsonUtils(ObjectProvider<ObjectMapper> objectMapperObjectProvider) {
        return new JsonUtils(objectMapperObjectProvider);
    }

    LayeredConnectionSocketFactory httpsSSLConnectionSocketFactory(Boolean disableSslValidation) {
        SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create().setTlsVersions(new TLS[]{TLS.V_1_0, TLS.V_1_1, TLS.V_1_2});
        if (Boolean.TRUE.equals(disableSslValidation)) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init((KeyManager[]) null, new TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }}, new SecureRandom());
                sslConnectionSocketFactoryBuilder.setSslContext(sslContext);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        } else {
            sslConnectionSocketFactoryBuilder.setSslContext(SSLContexts.createSystemDefault());
        }

        return sslConnectionSocketFactoryBuilder.build();
    }
}
