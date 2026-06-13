package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint.clients;

import io.github.lujiafa.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint.HintContext;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.web.client.RestClient;

public class HintRestClientCustomizer implements RestClientCustomizer {
    @Override
    public void customize(RestClient.Builder restClientBuilder) {
        restClientBuilder.requestInterceptor((request, body, execution) -> {
            HintContext.InnerHintData innerHintData = HintContext.get();
            if (innerHintData.getXHint() != null) {
                request.getHeaders().add(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME, innerHintData.getXHint());
            }
            return execution.execute(request, body);
        });

    }
}
