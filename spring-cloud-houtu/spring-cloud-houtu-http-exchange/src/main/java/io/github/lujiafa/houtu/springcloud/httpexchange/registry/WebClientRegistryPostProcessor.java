package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Registers {@link WebClient}-based HttpExchange client bean definitions and builds the client proxy
 * lazily at bean-instantiation time.
 */
public class WebClientRegistryPostProcessor extends HttpExchangeRegistryPostProcessor {

    @Override
    protected LinkedHashMap<String, Set<Class>> getRegistryMap(HttpExchangeRegistry registry) {
        return registry.getWebClientRegistryMap();
    }

    @Override
    protected Object instanceBeanObject(String baseUrl, String beanName, Class<?> serviceType) {
        WebClient.Builder builder = LoadBalancedBuilders.resolveLoadBalancedBuilder(beanFactory, WebClient.Builder.class);
        WebClient webClient = builder.clone().baseUrl(baseUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
        return factory.createClient(serviceType);
    }
}
