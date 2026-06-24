package io.github.lujiafa.houtu.cache.autoconfigure;

import io.github.lujiafa.houtu.cache.util.RedissonConnectionFactoryBeanUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.StringUtils;

/**
 * @author jon
 * @date 2020年12月17日
 */
@AutoConfigureAfter(DataRedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@EnableConfigurationProperties({DataRedisProperties.class})
public class CacheAutoConfiguration {

    @Configuration
    @ConditionalOnClass(org.redisson.api.RedissonClient.class)
    static class RedissonClientConfiguration {
        private static final Logger logger = LoggerFactory.getLogger(RedissonClientConfiguration.class);

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean
        public static org.redisson.api.RedissonClient redissonClient(Environment environment, DataRedisProperties redisProperties) {
            String config = environment.getProperty("spring.redis.redisson.config");
            if (StringUtils.hasLength(config)) {
                try {
                    return RedissonConnectionFactoryBeanUtils.redisson(config);
                } catch (Exception e) {
                    logger.warn("Failed to create RedissonClient from config string, falling back", e);
                }
            }
            String filePath = environment.getProperty("spring.redis.redisson.file");
            if (StringUtils.hasLength(filePath)) {
                try {
                    DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                    Resource resource = resourceLoader.getResource(filePath);
                    if (resource.exists()) {
                        return RedissonConnectionFactoryBeanUtils.redisson(resource.getFile());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to create RedissonClient from file '{}', falling back", filePath, e);
                }
            }
            return RedissonConnectionFactoryBeanUtils.redisson(redisProperties);
        }
    }

}
