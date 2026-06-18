package io.github.lujiafa.houtu.springcloud.feign.consumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.beans.Introspector;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class HttpExchangeRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    protected List<HttpExchangeCustomizer> httpExchangeCustomizers = new ArrayList<>();

    public HttpExchangeRegistryPostProcessor(List<HttpExchangeCustomizer> httpExchangeCustomizers) {
        if (httpExchangeCustomizers != null) {
            this.httpExchangeCustomizers.addAll(httpExchangeCustomizers);
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (httpExchangeCustomizers.isEmpty()) {
            return;
        }
        Map<String, Set<Class>> httpExchangeMap = httpExchangeCustomizers.stream().map(customizer -> {
                    HttpExchangeRegistry exchangeRegistry = new HttpExchangeRegistry();
                    customizer.customize(exchangeRegistry);
                    return exchangeRegistry;
                }).flatMap(r -> {
                    Map<String, Set<Class> > registryMap = new LinkedHashMap<>();
                    getRegistryMap(r).entrySet().stream().forEach(entry -> registryMap.put(parseBaseUrl(entry.getKey()), entry.getValue()));
                    return registryMap.entrySet().stream();
                })
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.flatMapping(e -> e.getValue().stream(), Collectors.toSet())));

        httpExchangeMap.forEach((key, classes) -> {
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(getHttpExchangeAdapter(key)).build();
            classes.forEach(clazz -> {
                String beanName = beanName(clazz);
                Object bean = factory.createClient(clazz);
                BeanDefinition definition = BeanDefinitionBuilder
                        .genericBeanDefinition(clazz, () -> bean)
                        .getBeanDefinition();
                registry.registerBeanDefinition(beanName, definition);
            });
        });
    }

    protected static String parseBaseUrl(String baseUrl) {
        if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))
            return baseUrl;
        return "http://" + baseUrl;
    }

    protected abstract LinkedHashMap<String, Set<Class>> getRegistryMap(HttpExchangeRegistry registry);

    protected abstract HttpExchangeAdapter getHttpExchangeAdapter(String baseUrl);

    protected String beanName(Class clazz) {
        return Introspector.decapitalize(clazz.getSimpleName());
    }
}