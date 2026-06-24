package io.github.lujiafa.houtu.cache.util;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.net.URI;

public final class LettuceConnectionFactoryBeanUtils {

    /**
     * 获取RedisConnectionFactory，主要适用于实例化对象到Spring容器中。
     * 参考：org.springframework.boot.data.redis.autoconfigure.RedisConnectionConfiguration、org.springframework.boot.data.redis.autoconfigure.JedisConnectionConfiguration
     * @param redisProperties redis配置
     * @param virtualThreads 是否使用虚拟线程
     * @return RedisConnectionFactory
     */
    public static RedisConnectionFactory getRedisConnectionFactory(DataRedisProperties redisProperties, boolean virtualThreads) {
        LettuceClientConfiguration clientConfiguration = getLettuceClientConfiguration(redisProperties);
        LettuceConnectionFactory connectionFactory;
        if (redisProperties.getSentinel() != null) {
            connectionFactory = new LettuceConnectionFactory(RedisConfigUtils.getSentinelConfig(redisProperties), clientConfiguration);
        } else if (redisProperties.getCluster() != null) {
            connectionFactory = new LettuceConnectionFactory(RedisConfigUtils.getClusterConfiguration(redisProperties), clientConfiguration);
        } else {
            connectionFactory = new LettuceConnectionFactory(RedisConfigUtils.getStandaloneConfig(redisProperties), clientConfiguration);
        }
        if (virtualThreads) {
            SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-lettuce-");
            executor.setVirtualThreads(true);
            connectionFactory.setExecutor(executor);
        }
        return connectionFactory;
    }

    private static LettuceClientConfiguration getLettuceClientConfiguration(DataRedisProperties redisProperties) {
        // 参考 LettuceConnectionConfiguration
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        boolean poolEnabled = redisProperties.getLettuce().getPool().getEnabled() != null ? redisProperties.getLettuce().getPool().getEnabled() : ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
                redisProperties.getClass().getClassLoader());
        if (poolEnabled) {
            GenericObjectPoolConfig<StatefulConnection<?, ?>> config = new GenericObjectPoolConfig<>();
            DataRedisProperties.Pool pool = redisProperties.getLettuce().getPool();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            if (pool.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
            }
            if (pool.getMaxWait() != null) {
                config.setMaxWait(pool.getMaxWait());
            }
            builder = LettucePoolingClientConfiguration.builder().poolConfig(config);
        } else {
            builder = LettuceClientConfiguration.builder();
        }
        SslBundle sslBundle = RedisConfigUtils.getSslBundle(redisProperties);
        if (sslBundle != null) {
            builder.useSsl();
        }
        if (StringUtils.hasLength(redisProperties.getUrl())) {
            try {
                URI uri = new URI(redisProperties.getUrl());
                if ("rediss".equals(uri.getScheme())) {
                    builder.useSsl();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot get Redis URL from '" + redisProperties.getUrl() + "'", e);
            }
        }
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }
        if (redisProperties.getLettuce() != null) {
            DataRedisProperties.Lettuce lettuce = redisProperties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(lettuce.getShutdownTimeout());
            }
            String readFrom = lettuce.getReadFrom();
            if (readFrom != null) {
                builder.readFrom(getReadFrom(readFrom));
            }
        }
        if (StringUtils.hasText(redisProperties.getClientName())) {
            builder.clientName(redisProperties.getClientName());
        }
        return builder.build();
    }

    private static ReadFrom getReadFrom(String readFrom) {
        int index = readFrom.indexOf(':');
        if (index == -1) {
            return ReadFrom.valueOf(getCanonicalReadFromName(readFrom));
        }
        String name = getCanonicalReadFromName(readFrom.substring(0, index));
        String value = readFrom.substring(index + 1);
        return ReadFrom.valueOf(name + ":" + value);
    }

    private static String getCanonicalReadFromName(String name) {
        StringBuilder canonicalName = new StringBuilder(name.length());
        name.chars()
                .filter(Character::isLetterOrDigit)
                .map(Character::toLowerCase)
                .forEach((c) -> canonicalName.append((char) c));
        return canonicalName.toString();
    }

}
