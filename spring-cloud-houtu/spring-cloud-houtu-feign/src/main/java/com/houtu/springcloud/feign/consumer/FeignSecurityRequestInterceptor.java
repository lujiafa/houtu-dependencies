package com.houtu.springcloud.feign.consumer;

import com.houtu.springcloud.feign.constant.FeignConstant;
import com.houtu.springcloud.feign.consumer.prop.FeignConsumerProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.annotation.Nonnull;

/**
 * feign请求安全参数设置拦截器，为上游服务设置安全校验参数
 * @author jonlu
 * @date 2022/9/23
 */
public class FeignSecurityRequestInterceptor implements RequestInterceptor {

    private FeignConsumerProperties feignConsumerProperties;

    public FeignSecurityRequestInterceptor(@Nonnull FeignConsumerProperties feignConsumerProperties) {
        this.feignConsumerProperties = feignConsumerProperties;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String secret = feignConsumerProperties.getSecrets().get(requestTemplate.feignTarget().name());
        if (secret == null) {
            secret = feignConsumerProperties.getSecret();
        }
        if (secret == null || secret.isEmpty()) return;
        requestTemplate.header(FeignConstant.FEIGN_REQUEST_SECRET_CODE, secret);
    }
}
