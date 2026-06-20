package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.beans.Introspector;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registers the microservice interfaces declared via {@link HttpExchangeCustomizer} as injectable client beans.
 *
 * <p>Design notes (fixing the silent load-balancing failure and related pitfalls):
 * <ul>
 *   <li>As a {@code BeanDefinitionRegistryPostProcessor}, this class only registers bean definitions early,
 *       so business classes can inject them via {@code @Autowired}/{@code @Resource};</li>
 *   <li>The real client is built lazily in the {@code instanceSupplier} callback at bean-instantiation time
 *       (after BeanPostProcessors are registered), by which point the {@code @LoadBalanced} builder already
 *       carries the LB interceptor;</li>
 *   <li>The bean name is the interface simple name, with the fully-qualified name registered as an alias
 *       (injectable by type / simple name / fully-qualified name).</li>
 * </ul>
 */
public abstract class HttpExchangeRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(HttpExchangeRegistryPostProcessor.class);

    protected ListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<HttpExchangeCustomizer> customizers = beanFactory.getBeanProvider(HttpExchangeCustomizer.class).orderedStream().toList();
        if (customizers.isEmpty()) {
            return;
        }

        Map<String, Set<Class>> httpExchangeMap = customizers.stream().map(customizer -> {
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
            classes.forEach(clazz -> {
                if (!clazz.isInterface()) {
                    throw new IllegalStateException("The registered HttpExchange client must be an interface, but got: " + clazz.getName());
                }
                String beanName = beanName(key, clazz);
                if (registry.containsBeanDefinition(beanName)) {
                    logger.warn("HttpExchange client bean definition '{}' already exists, skipping registration", beanName);
                    return;
                }
                GenericBeanDefinition definition = new GenericBeanDefinition();
                // beanClass is the interface itself, so the container can match @Autowired by type without instantiating it.
                definition.setBeanClass(clazz);
                // The client is built lazily at bean-instantiation time (instanceSupplier callback); by then
                // BeanPostProcessors are registered and the @LoadBalanced builder resolved by the subclass carries the LB interceptor.
                definition.setInstanceSupplier(() -> instanceBeanObject(key, beanName, clazz));
                definition.setAutowireCandidate(true);
                registry.registerBeanDefinition(beanName, definition);
                String fullBeanName = clazz.getName();
                if (registry.isBeanNameInUse(fullBeanName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("HttpExchange client alias '{}' is already in use, skipping alias", fullBeanName);
                    }
                    return;
                }
                registry.registerAlias(beanName, fullBeanName);
            });
        });
    }

    /** Subclass returns this type's (RestClient/WebClient) registry map from {@link HttpExchangeRegistry}. */
    protected abstract LinkedHashMap<String, Set<Class>> getRegistryMap(HttpExchangeRegistry registry);

    protected abstract Object instanceBeanObject(String baseUrl, String beanName, Class<?> serviceType);

    protected static String parseBaseUrl(String baseUrl) {
        if (baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            return baseUrl;
        }
        return "http://" + baseUrl;
    }

    protected String beanName(String baseUrl, Class<?> serviceType) {
        return Introspector.decapitalize(serviceType.getSimpleName());
    }

}
