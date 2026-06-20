package io.github.lujiafa.houtu.springcloud.httpexchange.registry;

@FunctionalInterface
public interface HttpExchangeCustomizer {

    void customize(HttpExchangeRegistry registry);
}
