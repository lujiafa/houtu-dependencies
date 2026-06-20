package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Registers {@link RestClient}-based HttpExchange client bean definitions and builds the client proxy
 * lazily at bean-instantiation time.
 */
public class RestClientRegistryPostProcessor extends HttpExchangeRegistryPostProcessor {

    @Override
    protected LinkedHashMap<String, Set<Class>> getRegistryMap(HttpExchangeRegistry registry) {
        return registry.getRestClientRegistryMap();
    }

    @Override
    protected Object instanceBeanObject(String baseUrl, String beanName, Class<?> serviceType) {
        RestClient.Builder builder = LoadBalancedBuilders.resolveLoadBalancedBuilder(beanFactory, RestClient.Builder.class);
        RestClient restClient = builder.clone().baseUrl(baseUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(serviceType);
    }
}
