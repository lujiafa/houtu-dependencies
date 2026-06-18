package io.github.lujiafa.houtu.springcloud.feign.consumer;

import org.springframework.util.Assert;

import java.util.*;

public class HttpExchangeRegistry {
    LinkedHashMap<String, Set<Class>> restClientRegistryMap = new LinkedHashMap<>();
    LinkedHashMap<String, Set<Class>> webClientRegistryMap = new LinkedHashMap<>();

    public LinkedHashMap<String, Set<Class>> getRestClientRegistryMap() {
        return restClientRegistryMap;
    }

    public LinkedHashMap<String, Set<Class>> getWebClientRegistryMap() {
        return webClientRegistryMap;
    }

    public HttpExchangeRegistry registry(String baseUrl, Class<?>... serviceClasses) {
        return registry(baseUrl, HttpExchangeType.REST_CLIENT, serviceClasses);
    }

    public HttpExchangeRegistry registry(String baseUrl, HttpExchangeType httpExchangeType, Class<?>... serviceClasses) {
        Assert.notNull(baseUrl, "baseUrl must not be null");
        Assert.notNull(serviceClasses, "serviceClasses must not be null");
        String trimmed = baseUrl.trim();
        if (trimmed.isEmpty()) {
            return this;
        }
        if (HttpExchangeType.WEB_CLIENT.equals(httpExchangeType)) {
            Set<Class> classes = webClientRegistryMap.computeIfAbsent(trimmed, k -> new LinkedHashSet<>());
            classes.addAll(Arrays.asList(serviceClasses));
        } else {
            Set<Class> classes = restClientRegistryMap.computeIfAbsent(trimmed, k -> new LinkedHashSet<Class>());
            classes.addAll(Arrays.asList(serviceClasses));
        }
        return this;
    }
}