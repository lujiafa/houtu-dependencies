package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint;

import io.github.lujiafa.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class HintWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String hint = request.getHeaders().getFirst(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME);
        if (hint != null) {
            HintContext.setX(hint);
        }
        return chain.filter(exchange).then(Mono.fromRunnable(HintContext::remove));
    }
}
