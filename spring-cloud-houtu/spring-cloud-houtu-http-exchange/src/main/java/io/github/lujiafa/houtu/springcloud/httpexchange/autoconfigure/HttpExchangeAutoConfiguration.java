package io.github.lujiafa.houtu.springcloud.httpexchange.autoconfigure;

import io.github.lujiafa.houtu.springcloud.httpexchange.condition.OnMissingLoadBalancedRestClientBuilder;
import io.github.lujiafa.houtu.springcloud.httpexchange.condition.OnMissingLoadBalancedWebClientBuilder;
import io.github.lujiafa.houtu.springcloud.httpexchange.registry.RestClientRegistryPostProcessor;
import io.github.lujiafa.houtu.springcloud.httpexchange.registry.WebClientRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration for the HttpExchange consumer side.
 *
 * <p>{@code after = RestClient/WebClientAutoConfiguration}: ensures the {@code RestClient.Builder}/{@code WebClient.Builder}
 * bean definitions appear before this configuration, so the {@code @ConditionalOnBean} checks below evaluate reliably.
 *
 * <p>Both registrars are declared as {@code static @Bean} and do not inject the builder: this avoids eagerly
 * instantiating the configuration class and the {@code @LoadBalanced} builder during the BeanFactoryPostProcessor
 * phase (which would run before the load-balancer BeanPostProcessor is registered, dropping the LB interceptor).
 * The real client is instead built lazily by the instanceSupplier at bean-instantiation time.
 */
@AutoConfiguration(after = {RestClientAutoConfiguration.class, WebClientAutoConfiguration.class})
public class HttpExchangeAutoConfiguration {

    @Configuration
    @ConditionalOnClass(RestClient.class)
    public static class RestClientExchangeConfiguration {

        @Bean(defaultCandidate = false)
        @LoadBalanced
        @ConditionalOnBean(RestClient.Builder.class)
        @Conditional(OnMissingLoadBalancedRestClientBuilder.class)
        public RestClient.Builder loadbalanceRestClientBuilder(RestClient.Builder restClientBuilder) {
            return restClientBuilder;
        }

        @Bean
        @ConditionalOnMissingBean
        public static RestClientRegistryPostProcessor restClientRegistryPostProcessor() {
            return new RestClientRegistryPostProcessor();
        }

    }

    @Configuration
    @ConditionalOnClass(WebClient.class)
    public static class WebClientExchangeConfiguration {

        @Bean(defaultCandidate = false)
        @LoadBalanced
        @ConditionalOnBean(WebClient.Builder.class)
        @Conditional(OnMissingLoadBalancedWebClientBuilder.class)
        public WebClient.Builder loadbalanceWebClientBuilder(WebClient.Builder builder) {
            return builder;
        }

        @Bean
        @ConditionalOnMissingBean
        public static WebClientRegistryPostProcessor webClientRegistryPostProcessor() {
            return new WebClientRegistryPostProcessor();
        }

    }

}
