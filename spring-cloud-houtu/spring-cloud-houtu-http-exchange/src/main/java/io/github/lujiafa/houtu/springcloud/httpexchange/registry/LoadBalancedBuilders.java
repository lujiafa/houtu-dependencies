package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

/**
 * Resolves the {@link LoadBalanced}-qualified builder bean of a matching type from the container
 * (e.g. {@code RestClient.Builder}, {@code WebClient.Builder}).
 */
public final class LoadBalancedBuilders {

    /**
     * Resolves the {@code @LoadBalanced} builder instance; throws with a clear reason if none exists.
     */
    public static <T> T resolveLoadBalancedBuilder(ListableBeanFactory beanFactory, Class<T> type) {
        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, type, true, false);
        for (String name : names) {
            if (beanFactory.findAnnotationOnBean(name, LoadBalanced.class) != null) {
                return beanFactory.getBean(name, type);
            }
        }
        throw new NoSuchBeanDefinitionException(type,
                "No @LoadBalanced " + type.getName() + " bean is available to build the HttpExchange client. "
                        + type.getSimpleName() + " is auto-configured only when its Spring Boot auto-configuration is active "
                        + "(for example, RestClient.Builder is not auto-configured in a reactive web application unless "
                        + "virtual threads are enabled). Enable it via 'spring.threads.virtual.enabled=true', declare your own "
                        + "@LoadBalanced " + type.getSimpleName() + " bean, or use the other client type.");
    }
}
