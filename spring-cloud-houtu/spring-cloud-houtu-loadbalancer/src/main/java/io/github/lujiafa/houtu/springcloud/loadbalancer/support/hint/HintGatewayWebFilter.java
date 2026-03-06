package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint;

import io.github.lujiafa.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import io.github.lujiafa.houtu.springcloud.loadbalancer.prop.SpringCloudLoadBalancerProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class HintGatewayWebFilter extends HintWebFilter implements WebFilter {

    private SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties;

    public HintGatewayWebFilter(SpringCloudLoadBalancerProperties springCloudLoadBalancerProperties) {
        this.springCloudLoadBalancerProperties = springCloudLoadBalancerProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (springCloudLoadBalancerProperties.isDisableGatewayRequestHint()) {
            ServerWebExchange removeRequestHintExchange = exchange.mutate()
                    .request(request -> request.headers(headers -> headers.remove(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME)))
                    .build();
            return chain.filter(removeRequestHintExchange).then(Mono.fromRunnable(HintContext::remove));
        }
        return super.filter(exchange, chain);
    }
}
