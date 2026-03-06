package io.github.lujiafa.houtu.springcloud.loadbalancer.support.hint;

import io.github.lujiafa.houtu.springcloud.loadbalancer.constant.LoadBalancerConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class HintFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        HintContext.InnerHintData innerHintData = HintContext.get();
        if (innerHintData.getXHint() != null) {
            requestTemplate.header(LoadBalancerConstant.REQUEST_CONTEXT_HINT_NAME, innerHintData.getXHint());
        }
    }

}
