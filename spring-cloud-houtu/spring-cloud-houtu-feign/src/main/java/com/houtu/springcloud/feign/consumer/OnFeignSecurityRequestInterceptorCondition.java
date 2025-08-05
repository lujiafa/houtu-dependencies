package com.houtu.springcloud.feign.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

public class OnFeignSecurityRequestInterceptorCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String secret = context.getEnvironment().getProperty("spring.cloud.feign.consumer.secret", String.class);
        Map<String, String> secrets = context.getEnvironment().getProperty("spring.cloud.feign.consumer.secrets", Map.class);
        if ((secret != null && !secret.isEmpty()) || (secrets != null && !secrets.isEmpty()))
            return ConditionOutcome.match();
        return ConditionOutcome.noMatch("No feign secret properties");
    }
}
