package com.houtu.springcloud.discovery.health;


import com.houtu.springcloud.discovery.constant.DiscoveryConstant;
import com.houtu.springcloud.discovery.context.ServiceContext;
import com.houtu.springcloud.discovery.type.ServiceStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class ReactiveServiceHealthWebFilter implements WebFilter {

    private ServiceContext serviceContext;

    public ReactiveServiceHealthWebFilter(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (DiscoveryConstant.COMMON_HEALTH_PATH.equals(exchange.getRequest().getURI().getPath())) {
            exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
            if (ServiceStatus.UP.equals(serviceContext.getServiceState())) {
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("{\"status\":\"UP\"}".getBytes())));
            } else {
                return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("{\"status\":\"DOWN\"}".getBytes())));
            }
        }
        return chain.filter(exchange);
    }
}
