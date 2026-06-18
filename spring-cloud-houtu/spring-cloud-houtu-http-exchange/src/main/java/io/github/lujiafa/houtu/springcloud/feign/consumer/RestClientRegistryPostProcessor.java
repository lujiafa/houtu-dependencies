package io.github.lujiafa.houtu.springcloud.feign.consumer;

import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestClientRegistryPostProcessor extends HttpExchangeRegistryPostProcessor {

    private final RestClient.Builder restClientBuilder;

    public RestClientRegistryPostProcessor(RestClient.Builder restClientBuilder, List<HttpExchangeCustomizer> httpExchangeCustomizers) {
        super(httpExchangeCustomizers);
        Assert.notNull(restClientBuilder, "restClientBuilder must not be null");
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    protected LinkedHashMap<String, Set<Class>> getRegistryMap(HttpExchangeRegistry registry) {
        return registry.getRestClientRegistryMap();
    }

    @Override
    protected HttpExchangeAdapter getHttpExchangeAdapter(String baseUrl) {
        RestClient restClient = restClientBuilder.baseUrl(baseUrl).build();
        return RestClientAdapter.create(restClient);
    }
}