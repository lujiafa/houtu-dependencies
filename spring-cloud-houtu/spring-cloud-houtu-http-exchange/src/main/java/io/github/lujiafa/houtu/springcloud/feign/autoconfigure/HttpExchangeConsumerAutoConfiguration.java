package io.github.lujiafa.houtu.springcloud.feign.autoconfigure;

import io.github.lujiafa.houtu.springcloud.feign.condition.OnMissingLoadBalancedRestClientBuilder;
import io.github.lujiafa.houtu.springcloud.feign.condition.OnMissingLoadBalancedWebClientBuilder;
import io.github.lujiafa.houtu.springcloud.feign.consumer.HttpExchangeCustomizer;
import io.github.lujiafa.houtu.springcloud.feign.consumer.RestClientRegistryPostProcessor;
import io.github.lujiafa.houtu.springcloud.feign.consumer.WebClientRegistryPostProcessor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
//        (
//                after = {RestClientAutoConfiguration.class, WebClientAutoConfiguration.class}
//        )
public class HttpExchangeConsumerAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RestClient.class)
    public static class RestClientExchangeConfiguration {

        @Bean
        @LoadBalanced
        @ConditionalOnBean(RestClient.Builder.class)
        @Conditional(OnMissingLoadBalancedRestClientBuilder.class)
        public RestClient.Builder loadbalanceRestClientBuilder(RestClient.Builder builder) {
            return builder;
        }

        @Bean
        @ConditionalOnBean(RestClient.Builder.class)
        @ConditionalOnMissingBean
        public RestClientRegistryPostProcessor restClientRegistryPostProcessor(@LoadBalanced RestClient.Builder loadbalanceRestClientBuilder,
                                                                              ObjectProvider<CloseableHttpClient> httpClientObjectProvider,
                                                                              ObjectProvider<HttpExchangeCustomizer> httpExchangeCustomizers) {
            CloseableHttpClient httpClient = httpClientObjectProvider.getIfUnique();
            if (httpClient != null) {
                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
                loadbalanceRestClientBuilder.requestFactory(factory);
            }
            return new RestClientRegistryPostProcessor(loadbalanceRestClientBuilder, httpExchangeCustomizers.orderedStream().toList());
        }

    }

    @Configuration
    @ConditionalOnClass(WebClient.class)
    public static class WebClientExchangeConfiguration {

        @Bean
        @LoadBalanced
        @ConditionalOnBean(WebClient.Builder.class)
        @Conditional(OnMissingLoadBalancedWebClientBuilder.class)
        public WebClient.Builder loadbalanceWebClientBuilder(WebClient.Builder builder) {
            return builder;
        }

        @Bean
        @ConditionalOnBean(WebClient.Builder.class)
        @ConditionalOnMissingBean
        public WebClientRegistryPostProcessor webClientRegistryPostProcessor(@LoadBalanced WebClient.Builder loadbalanceWebClientBuilder,
                                                                              ObjectProvider<HttpExchangeCustomizer> httpExchangeCustomizers) {
            return new WebClientRegistryPostProcessor(loadbalanceWebClientBuilder, httpExchangeCustomizers.orderedStream().toList());
        }

    }

}
