package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.annotation.GetExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies clone() isolation: when building a client, baseUrl must be set on a copy of the {@code @LoadBalanced}
 * builder and built from that copy, never mutating the shared builder in place — so multiple clients do not
 * pollute each other under concurrency / multiple base URLs.
 *
 * <p>Calls {@code instanceBeanObject} (a package-visible protected method) directly to verify the logic in
 * isolation, without starting a container.
 */
class RestClientRegistryPostProcessorTest {

    @Test
    void buildsClientFromClonedBuilderWithoutMutatingSharedBuilder() {
        RestClient.Builder sharedBuilder = mock(RestClient.Builder.class);
        RestClient.Builder clonedBuilder = mock(RestClient.Builder.class);
        when(sharedBuilder.clone()).thenReturn(clonedBuilder);
        when(clonedBuilder.baseUrl("http://order-service")).thenReturn(clonedBuilder);
        when(clonedBuilder.build()).thenReturn(mock(RestClient.class));

        ListableBeanFactory beanFactory = mock(ListableBeanFactory.class);
        when(beanFactory.getBeanNamesForType(RestClient.Builder.class, true, false))
                .thenReturn(new String[]{"loadbalanceRestClientBuilder"});
        when(beanFactory.findAnnotationOnBean("loadbalanceRestClientBuilder", LoadBalanced.class))
                .thenReturn(mock(LoadBalanced.class));
        when(beanFactory.getBean("loadbalanceRestClientBuilder", RestClient.Builder.class))
                .thenReturn(sharedBuilder);

        RestClientRegistryPostProcessor processor = new RestClientRegistryPostProcessor();
        processor.setBeanFactory(beanFactory);

        Object client = processor.instanceBeanObject("http://order-service", "orderClient", OrderClient.class);

        assertThat(client).isInstanceOf(OrderClient.class);
        verify(sharedBuilder).clone();
        verify(clonedBuilder).baseUrl("http://order-service");
        verify(clonedBuilder).build();
        // the shared builder must never be mutated in place
        verify(sharedBuilder, never()).baseUrl(anyString());
        verify(sharedBuilder, never()).build();
    }

    public interface OrderClient {
        @GetExchange("/orders")
        String list();
    }
}
