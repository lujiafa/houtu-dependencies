package com.houtu.springcloud.feign.handler;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class CustomRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // do nothing
    }
}
