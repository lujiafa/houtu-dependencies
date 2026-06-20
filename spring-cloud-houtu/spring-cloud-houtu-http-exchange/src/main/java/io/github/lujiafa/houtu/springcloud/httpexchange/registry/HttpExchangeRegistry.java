package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.web.service.annotation.HttpExchange;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class HttpExchangeRegistry {
    private final LinkedHashMap<String, Set<Class>> restClientRegistryMap = new LinkedHashMap<>();
    private final LinkedHashMap<String, Set<Class>> webClientRegistryMap = new LinkedHashMap<>();

    public LinkedHashMap<String, Set<Class>> getRestClientRegistryMap() {
        return restClientRegistryMap;
    }

    public LinkedHashMap<String, Set<Class>> getWebClientRegistryMap() {
        return webClientRegistryMap;
    }

    public HttpExchangeRegistry registryRestClient(String baseUrl, Class<?>... serviceClasses) {
        return registry(baseUrl, HttpExchangeType.REST_CLIENT, serviceClasses);
    }
    public HttpExchangeRegistry registryWebClient(String baseUrl, Class<?>... serviceClasses) {
        return registry(baseUrl, HttpExchangeType.WEB_CLIENT, serviceClasses);
    }

    public HttpExchangeRegistry registry(String baseUrl, HttpExchangeType httpExchangeType, Class<?>... serviceClasses) {
        Assert.hasText(baseUrl, "baseUrl must not be empty");
        Assert.isTrue(serviceClasses != null && serviceClasses.length > 0, "serviceClasses must not be empty");
        Assert.notNull(httpExchangeType, "httpExchangeType must not be null");
        for (Class<?> serviceClass : serviceClasses) {
            Assert.notNull(serviceClass, "serviceClasses must not contain null elements");
            if (!serviceClass.isInterface()) {
                throw new IllegalArgumentException("HttpExchange service class must be an interface, but got: " + serviceClass.getName());
            }
            if (!isHttpExchangeInterface(serviceClass)) {
                throw new IllegalArgumentException("HttpExchange service interface must be annotated with @HttpExchange on the type "
                                + "or declare at least one method annotated with @HttpExchange (e.g. @GetExchange, "
                                + "@PostExchange), but none was found on: " + serviceClass.getName());
            }
        }
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

    /**
     * Determines whether the given interface qualifies as an HttpExchange proxy target, i.e. it carries
     * {@link HttpExchange} on the type itself or declares at least one method (meta-)annotated with
     * {@link HttpExchange} (such as {@code @GetExchange} or {@code @PostExchange}).
     */
    private boolean isHttpExchangeInterface(Class<?> serviceClass) {
        if (AnnotatedElementUtils.hasAnnotation(serviceClass, HttpExchange.class)) {
            return true;
        }
        for (Method method : serviceClass.getMethods()) {
            if (AnnotatedElementUtils.hasAnnotation(method, HttpExchange.class)) {
                return true;
            }
        }
        return false;
    }
}