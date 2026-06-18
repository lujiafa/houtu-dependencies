package io.github.lujiafa.houtu.springcloud.feign.condition;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.client.RestClient;

/**
 * 当容器中已存在带 {@link LoadBalanced} 限定符的 {@link RestClient.Builder} Bean（任意 Bean 名）时条件不成立，
 * 用于让自动配置的 loadbalanceRestClientBuilder 在用户已自定义负载均衡 Builder 时回退。
 *
 * <p>{@code @ConditionalOnMissingBean(annotation = LoadBalanced.class)} 仅按注解匹配、不限定类型，
 * 会被其它 {@code @LoadBalanced} Bean（如 WebClient.Builder、RestTemplate）误触发；
 * 此 Condition 同时校验「类型为 RestClient.Builder」且「带 @LoadBalanced」。
 */
public class OnMissingLoadBalancedRestClientBuilder implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory == null) {
            return true;
        }
        // allowEagerInit=false：仅按定义/工厂方法返回类型匹配，不会触发 Bean 实例化
        String[] names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                beanFactory, RestClient.Builder.class, true, false);
        for (String name : names) {
            if (beanFactory.findAnnotationOnBean(name, LoadBalanced.class) != null) {
                return false;
            }
        }
        return true;
    }
}
