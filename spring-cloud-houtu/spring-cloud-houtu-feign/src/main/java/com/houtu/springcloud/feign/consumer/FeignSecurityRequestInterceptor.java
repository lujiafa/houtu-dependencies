package com.houtu.springcloud.feign.consumer;

import com.houtu.springcloud.feign.constant.FeignConstant;
import com.houtu.springcloud.feign.consumer.prop.FeignConsumerProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.annotation.Nonnull;

/**
 * Feign异常记录器
 * @author jonlu
 * @date 2022/9/23
 */
public class FeignSecurityRequestInterceptor implements RequestInterceptor {


    public FeignSecurityRequestInterceptor() {
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.request();
    }
}
