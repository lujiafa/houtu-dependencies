package io.github.lujiafa.houtu.springcloud.feign.consumer;

import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebClientRegistryPostProcessor extends HttpExchangeRegistryPostProcessor {

    private final WebClient.Builder webClientBuilder;

    public WebClientRegistryPostProcessor(WebClient.Builder webClientBuilder, List<HttpExchangeCustomizer> httpExchangeCustomizers) {
        super(httpExchangeCustomizers);
        Assert.notNull(webClientBuilder, "webClientBuilder must not be null");
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    protected LinkedHashMap<String, Set<Class>> getRegistryMap(HttpExchangeRegistry registry) {
        return registry.getWebClientRegistryMap();
    }

    @Override
    protected HttpExchangeAdapter getHttpExchangeAdapter(String baseUrl) {
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
        return WebClientAdapter.create(webClient);
    }
}