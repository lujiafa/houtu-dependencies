package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint.clients;

import io.github.lujiafa.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint.HintContext;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

public class HintWebClientCustomizer implements WebClientCustomizer {

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        webClientBuilder.filter((request, next) -> {
            HintContext.InnerHintData innerHintData = HintContext.get();
            if (innerHintData.getXHint() != null) {
                ClientRequest newRequest = ClientRequest.from(request)
                        .header(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME, innerHintData.getXHint())
                        .build();
                return next.exchange(newRequest);
            }
            return next.exchange(request);
        });
    }
}
