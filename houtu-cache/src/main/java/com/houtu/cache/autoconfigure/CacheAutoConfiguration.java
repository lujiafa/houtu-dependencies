package com.houtu.cache.autoconfigure;

import com.houtu.cache.util.RedissonConnectionFactoryBeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.StringUtils;

/**
 * @author jon
 * @date 2020年12月17日
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@EnableConfigurationProperties({RedisProperties.class})
public class CacheAutoConfiguration {

    @Configuration
    @ConditionalOnClass(org.redisson.api.RedissonClient.class)
    static class RedissonClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public static org.redisson.api.RedissonClient redissonClient(Environment environment, RedisProperties redisProperties) {
            String config = environment.getProperty("spring.redis.redisson.config");
            if (StringUtils.hasLength(config)) {
                try {
                    return RedissonConnectionFactoryBeanUtils.redisson(config);
                } catch (Exception e) {}
            }
            String filePath = environment.getProperty("spring.redis.redisson.file");
            if (StringUtils.hasLength(filePath)) {
                try {
                    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                    Resource resource = resourceLoader.getResource(filePath);
                    if (resource.exists()) {
                        return RedissonConnectionFactoryBeanUtils.redisson(resource.getFile());
                    }
                } catch (Exception e) {}
            }
            return RedissonConnectionFactoryBeanUtils.redisson(redisProperties);
        }
    }

}
