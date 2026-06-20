package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Core correctness test: a client registered via {@link HttpExchangeCustomizer} that points at a service-id
 * must be resolved by the load-balancer interceptor from {@code http://test-service} to a real backend instance
 * and called successfully. Guards against the regression where the client is built during the
 * BeanFactoryPostProcessor phase and silently loses the LB interceptor.
 */
@SpringBootTest(classes = HttpExchangeLoadBalancerIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        // houtu loadbalancer's default LB client config couples to Nacos (an optional dependency not on the test
        // classpath), so it is excluded here in favor of Spring Cloud's native default LB
        // (RoundRobinLoadBalancer + the stub supplier below).
        properties = "spring.autoconfigure.exclude=io.github.lujiafa.houtu.springcloud.loadbalancer.autoconfigure.SpringCloudLoadBalancerAutoConfiguration")
class HttpExchangeLoadBalancerIntegrationTest {

    /** The backend port is only known after the context starts; the LB stub reads it lazily at request time. */
    static final AtomicReference<Integer> BACKEND_PORT = new AtomicReference<>();

    @LocalServerPort
    int port;

    @Autowired
    PingClient pingClient;

    @Test
    void loadBalancedClientResolvesServiceIdToBackend() {
        BACKEND_PORT.set(port);

        assertThat(pingClient.ping()).isEqualTo("pong");
    }

    public interface PingClient {
        @GetExchange("/ping")
        String ping();
    }

    @RestController
    static class PingController {
        @GetMapping("/ping")
        public String ping() {
            return "pong";
        }
    }

    @Configuration
    @EnableAutoConfiguration
    @LoadBalancerClient(name = "test-service", configuration = TestServiceLbConfig.class)
    static class TestApp {

        @Bean
        PingController pingController() {
            return new PingController();
        }

        @Bean
        HttpExchangeCustomizer pingCustomizer() {
            return registry -> registry.registryRestClient("http://test-service", PingClient.class);
        }
    }
}

/**
 * Load-balancer child-context configuration for "test-service": always resolves to the in-process embedded server port.
 * Intentionally not annotated with {@code @Configuration} so it is not picked up by the parent context's component scan.
 */
class TestServiceLbConfig {

    @Bean
    ServiceInstanceListSupplier testServiceInstanceSupplier() {
        return new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return "test-service";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                Integer port = HttpExchangeLoadBalancerIntegrationTest.BACKEND_PORT.get();
                return Flux.just(List.of(
                        new DefaultServiceInstance("test-service-1", "test-service", "127.0.0.1", port, false)));
            }
        };
    }
}
