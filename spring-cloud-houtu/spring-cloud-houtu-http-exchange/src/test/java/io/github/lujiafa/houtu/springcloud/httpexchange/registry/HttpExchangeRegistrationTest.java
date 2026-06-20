package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

import io.github.lujiafa.houtu.springcloud.httpexchange.autoconfigure.HttpExchangeAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.service.annotation.GetExchange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Registration / naming / validation behavior tests, verified lightly via {@link ApplicationContextRunner}
 * without starting a web server or loading the LB infrastructure (the instanceSupplier only builds the proxy,
 * it makes no network call).
 *
 * <p>Assertions reflect the current implementation: beans are registered under the interface simple name with the
 * fully-qualified name as an alias and are injectable by type; non-interfaces fail fast; when the same interface
 * is mapped to multiple base URLs, duplicates are skipped without throwing.
 */
class HttpExchangeRegistrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RestClientAutoConfiguration.class,
                    HttpExchangeAutoConfiguration.class));

    @Test
    void registersClientInjectableByTypeShortNameAndFqn() {
        runner.withBean("pingCustomizer", HttpExchangeCustomizer.class,
                        () -> registry -> registry.registryRestClient("http://test-service", PingClient.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(PingClient.class);
                    // by type
                    assertThat(context.getBean(PingClient.class)).isNotNull();
                    // by simple name
                    assertThat(context.getBean("pingClient")).isInstanceOf(PingClient.class);
                    // fully-qualified name registered as alias -> injectable by FQN
                    assertThat(context.getBean(PingClient.class.getName())).isInstanceOf(PingClient.class);
                });
    }

    @Test
    void failsFastWhenServiceTypeIsNotInterface() {
        runner.withBean("badCustomizer", HttpExchangeCustomizer.class,
                        () -> registry -> registry.registryRestClient("http://test-service", NotAnInterface.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().hasStackTraceContaining("must be an interface");
                });
    }

    @Test
    void whenSameInterfaceMappedToMultipleBaseUrls_registersOnceAndSkipsTheRest() {
        runner.withBean("conflictCustomizer", HttpExchangeCustomizer.class,
                        () -> registry -> {
                            registry.registryRestClient("http://service-a", PingClient.class);
                            registry.registryRestClient("http://service-b", PingClient.class);
                        })
                .run(context -> {
                    // the current approach does not fail fast: on a duplicate simple name it skips the rest and
                    // keeps only one (which base URL wins is non-deterministic)
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(PingClient.class);
                });
    }

    @Test
    void registersNoClientBeansWithoutCustomizer() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(PingClient.class);
        });
    }

    @Test
    void failsFastWithActionableMessageWhenRestClientBuilderIsAbsent() {
        // No RestClientAutoConfiguration -> no RestClient.Builder bean (simulates a reactive app without virtual threads).
        // A declared RestClient client must then fail fast at startup with an actionable message rather than silently disappear.
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(HttpExchangeAutoConfiguration.class))
                .withBean("pingCustomizer", HttpExchangeCustomizer.class,
                        () -> registry -> registry.registryRestClient("http://test-service", PingClient.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().hasStackTraceContaining("spring.threads.virtual.enabled");
                });
    }

    public interface PingClient {
        @GetExchange("/ping")
        String ping();
    }

    public static class NotAnInterface {
    }
}
