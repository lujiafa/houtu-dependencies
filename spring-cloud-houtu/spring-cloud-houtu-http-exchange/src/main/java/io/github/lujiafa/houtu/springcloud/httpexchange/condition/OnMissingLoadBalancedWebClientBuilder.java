package io.github.lujiafa.houtu.springcloud.httpexchange.condition;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Does not match when a {@link WebClient.Builder} bean qualified with {@link LoadBalanced} (under any bean name)
 * already exists in the container, allowing the auto-configured {@code loadbalanceWebClientBuilder} to back off
 * when the user has supplied a custom load-balanced builder.
 *
 * <p>{@code @ConditionalOnMissingBean(annotation = LoadBalanced.class)} matches by annotation only, regardless of
 * type, and would be falsely triggered by other {@code @LoadBalanced} beans (e.g. WebClient.Builder, RestTemplate);
 * this Condition checks both "type is WebClient.Builder" and "annotated with @LoadBalanced".
 */
public class OnMissingLoadBalancedWebClientBuilder implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory == null) {
            return true;
        }
        // allowEagerInit=false: match by definition/factory-method return type only, without triggering bean instantiation
        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                beanFactory, WebClient.Builder.class, true, false);
        for (String name : names) {
            if (beanFactory.findAnnotationOnBean(name, LoadBalanced.class) != null) {
                return false;
            }
        }
        return true;
    }
}
