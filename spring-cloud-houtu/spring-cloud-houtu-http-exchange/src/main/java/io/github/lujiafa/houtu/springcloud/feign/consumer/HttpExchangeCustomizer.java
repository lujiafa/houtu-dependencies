package io.github.lujiafa.houtu.springcloud.feign.consumer;

@FunctionalInterface
public interface HttpExchangeCustomizer {

    void customize(HttpExchangeRegistry registry);
}
