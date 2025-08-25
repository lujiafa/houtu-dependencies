package com.houtu.lock.autoconfigure;

import com.houtu.cache.autoconfigure.CacheAutoConfiguration;
import com.houtu.lock.aspect.RedisLockAspect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfigureAfter(CacheAutoConfiguration.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class LockAutoConfiguration {

    @Configuration
    @ConditionalOnClass({org.aspectj.weaver.Advice.class, org.redisson.api.RedissonClient.class})
    static class AspectLockConfiguration {
        @Bean
        @ConditionalOnBean(org.redisson.api.RedissonClient.class)
        @ConditionalOnMissingBean
        public RedisLockAspect redisLockAspect() {
            return new RedisLockAspect();
        }
    }
}
