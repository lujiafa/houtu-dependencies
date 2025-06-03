package com.houtu.springcloud.loadbalancer.support.hint;

import com.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class HintWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HintContext.set(request.getHeaders().getFirst(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME));
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HintContext.remove();
        }));
    }
}
